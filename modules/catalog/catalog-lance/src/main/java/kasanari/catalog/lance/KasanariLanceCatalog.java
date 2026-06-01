package kasanari.catalog.lance;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.lance.NamespaceRepository;
import kasanari.repository.lance.TableRepository;
import kasanari.repository.lance.model.TableMetadata;
import kasanari.repository.lance.postgres.JdbcNamespaceRepository;
import kasanari.repository.lance.postgres.JdbcTableInitializer;
import kasanari.repository.lance.postgres.JdbcTableRepository;
import org.apache.arrow.memory.BufferAllocator;
import org.jdbi.v3.core.Handle;
import org.lance.Dataset;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.errors.NamespaceAlreadyExistsException;
import org.lance.namespace.errors.NamespaceNotEmptyException;
import org.lance.namespace.errors.NamespaceNotFoundException;
import org.lance.namespace.errors.TableAlreadyExistsException;
import org.lance.namespace.errors.TableNotFoundException;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableAlterColumnsResponse;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsResponse;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateNamespaceResponse;
import org.lance.namespace.model.CreateTableRequest;
import org.lance.namespace.model.CreateTableResponse;
import org.lance.namespace.model.DeclareTableRequest;
import org.lance.namespace.model.DeclareTableResponse;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DeregisterTableResponse;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceResponse;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DescribeTableResponse;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.DropNamespaceResponse;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.DropTableResponse;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RegisterTableResponse;
import org.lance.namespace.model.RenameTableRequest;
import org.lance.namespace.model.RenameTableResponse;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.RestoreTableResponse;
import org.lance.namespace.model.TableExistsRequest;
import org.lance.schema.ColumnAlteration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static kasanari.core.Functions.mapOrEmpty;
import static kasanari.core.Functions.valueOrDefault;

public class KasanariLanceCatalog implements LanceNamespace, AutoCloseable {
    private KasanariDataSource dataSource;
    private BufferAllocator allocator;
    private NamespaceRepository<Handle> namespaceRepository;
    private TableRepository<Handle> tableRepository;
    private TransactionManager<Handle> transactionManager;

    private String defaultLocation;

    @Override
    public void initialize(Map<String, String> properties, BufferAllocator allocator) {
        this.allocator = allocator;
        this.dataSource = new KasanariDataSource(properties);
        this.transactionManager = new JdbcTransactionManager(dataSource);
        new JdbcTableInitializer(transactionManager).initialize();

        this.namespaceRepository = new JdbcNamespaceRepository();
        this.tableRepository = new JdbcTableRepository();

        this.defaultLocation = properties.get(KasanariLanceProperties.LOCATION);

        if (defaultLocation == null) {
            throw new IllegalArgumentException("Default location is not set");
        }
    }

    @Override
    public String namespaceId() {
        return "kasanari-lance";
    }

    @Override
    public CreateNamespaceResponse createNamespace(CreateNamespaceRequest request) {
        var namespacePath = fullObjectName(request.getId());
        var exists = transactionManager.inTransactionR(tx -> namespaceRepository.exists(tx, namespacePath));
        var mode = valueOrDefault(request.getMode(), "create").toLowerCase();

        if (exists) {
            switch (mode) {
                case "existok", "exist_ok": return new CreateNamespaceResponse().properties(mapOrEmpty(request.getProperties()));
                case "create": throw new NamespaceAlreadyExistsException(String.format("Namespace %s already exists", namespacePath));
                case "overwrite": {
                    // the existing namespace is dropped and a new empty namespace with this name is created.
                    transactionManager.inTransaction(tx -> {
                        var tables = tableRepository.listByNamespace(tx, namespacePath);

                        for (var tableId : tables) {
                            var maybeTable = tableRepository.get(tx, tableId);
                            if (maybeTable.isPresent()) {
                                tableRepository.delete(tx, tableId);
                            }
                        }

                        namespaceRepository.delete(tx, namespacePath);
                    });
                }
            }
        }

        transactionManager.inTransaction(tx -> namespaceRepository.upsert(tx, namespacePath, mapOrEmpty(request.getProperties())));
        return new CreateNamespaceResponse().properties(mapOrEmpty(request.getProperties()));
    }

    @Override
    public DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request) {
        var namespacePath = fullObjectName(request.getId());
        var maybeProperties = transactionManager.inTransactionR(tx -> namespaceRepository.properties(tx, namespacePath));

        if (maybeProperties.isPresent()) {
            return new DescribeNamespaceResponse().properties(maybeProperties.get());
        }

        throw new NamespaceNotFoundException(String.format("Namespace %s does not exist", namespacePath));
    }

    @Override
    public DropNamespaceResponse dropNamespace(DropNamespaceRequest request) {
        var namespacePath = fullObjectName(request.getId());
        var mode = valueOrDefault(request.getMode(), "fail").toLowerCase();
        var behavior = valueOrDefault(request.getBehavior(), "restrict").toLowerCase();

        var maybeNamespace = transactionManager.inTransactionR(tx -> namespaceRepository.properties(tx, namespacePath));

        if (maybeNamespace.isEmpty()) {
            switch (mode) {
                case "fail": throw new NamespaceNotFoundException(String.format("Namespace %s does not exist", namespacePath));
                case "skip": return new DropNamespaceResponse();
            }
        }

        switch (behavior) {
            case "restrict": {
                transactionManager.inTransaction(tx -> {
                    var tables = tableRepository.listByNamespace(tx, namespacePath);
                    if (!tables.isEmpty()) {
                        throw new NamespaceNotEmptyException(String.format("Not empty namespace: %s", namespacePath));
                    }

                    namespaceRepository.delete(tx, namespacePath);
                });
            }
            case "cascade": {
                transactionManager.inTransaction(tx -> {
                    var tables = tableRepository.listByNamespace(tx, namespacePath);

                    for (var tableId : tables) {
                        var maybeTable = tableRepository.get(tx, tableId);
                        if (maybeTable.isPresent()) {
                            tableRepository.delete(tx, tableId);
                        }
                    }

                    namespaceRepository.delete(tx, namespacePath);
                });
            }
            default: throw new IllegalArgumentException("Unknown behaviour");
        }
    }

    @Override
    public void namespaceExists(NamespaceExistsRequest request) {
        var namespacePath = fullObjectName(request.getId());
        var exists = transactionManager.inTransactionR(tx -> namespaceRepository.exists(tx, namespacePath));

        if (!exists) {
            throw new NamespaceNotFoundException(String.format("Namespace %s does not exist", namespacePath));
        }
    }

    // todo: pagination
    @Override
    public ListNamespacesResponse listNamespaces(ListNamespacesRequest request) {
        var parent = fullObjectName(request.getId());
        var children = transactionManager.inTransactionR(tx -> namespaceRepository.list(tx, parent));
        var response = new ListNamespacesResponse();
        response.setNamespaces(new java.util.LinkedHashSet<>(children));
        response.setPageToken(null);
        return response;
    }

    // todo: pagination
    @Override
    public ListTablesResponse listTables(ListTablesRequest request) {
        var namespacePath = fullObjectName(request.getId());
        var tables = transactionManager.inTransactionR(tx -> tableRepository.listByNamespace(tx, namespacePath));
        var response = new ListTablesResponse();
        response.setTables(new java.util.LinkedHashSet<>(tables));
        response.setPageToken(null);
        return response;
    }

    @Override
    public RegisterTableResponse registerTable(RegisterTableRequest request) {
        var tableId = fullObjectName(request.getId());
        var namespacePath = namespaceFrom(tableId);
        var tableName = tableNameFrom(tableId);
        var mode = valueOrDefault(request.getMode(), "create").toLowerCase();

        transactionManager.inTransaction(tx -> {
            var maybeTable = tableRepository.get(tx, tableId);

            if (maybeTable.isPresent()) {
                if (mode.equals("create")) {
                    throw new TableAlreadyExistsException(String.format("Table %s already exists", tableId));
                }
            }

            tableRepository.upsert(
                    tx,
                    tableId,
                    namespacePath,
                    tableName,
                    request.getLocation(),
                    mapOrEmpty(request.getProperties())
            );
        });

        return new RegisterTableResponse()
                .location(request.getLocation())
                .properties(mapOrEmpty(request.getProperties()));
    }

    @Override
    public DescribeTableResponse describeTable(DescribeTableRequest request) {
        var tableId = fullObjectName(request.getId());
        var maybeTable = transactionManager.inTransactionR(tx -> tableRepository.get(tx, tableId));

        if (maybeTable.isPresent()) {
            var table = maybeTable.get();
            var namespace = Arrays.stream(table.namespacePath().split("[.]")).toList();

            return new DescribeTableResponse()
                    .table(table.tableName())
                    .namespace(namespace)
                    .location(table.location())
                    .properties(table.properties());
        }

        throw new TableNotFoundException(String.format("Table %s not found", tableId));
    }

    @Override
    public void tableExists(TableExistsRequest request) {
        var tableId = fullObjectName(request.getId());
        var exists = transactionManager.inTransactionR(tx -> tableRepository.exists(tx, tableId));
        if (!exists) {
            throw new IllegalStateException("Table does not exist: " + tableId);
        }
    }

    @Override
    public DropTableResponse dropTable(DropTableRequest request) {
        var tableId = fullObjectName(request.getId());
        var metadata = deleteTableMetadata(tableId);
        // delete table data
        purgeTableData(metadata.location(), metadata.properties());

        return new DropTableResponse()
                .id(request.getId())
                .location(metadata.location())
                .properties(metadata.properties());
    }

    @Override
    public DeregisterTableResponse deregisterTable(DeregisterTableRequest request) {
        var tableId = fullObjectName(request.getId());
        // delete only metadata
        var metadata = deleteTableMetadata(tableId);

        return new DeregisterTableResponse()
                .id(request.getId())
                .location(metadata.location())
                .properties(metadata.properties());
    }

    @Override
    public CreateTableResponse createTable(CreateTableRequest request, byte[] requestData) {
        var mode = valueOrDefault(request.getMode(), "create").toLowerCase();
        var tableId = fullObjectName(request.getId());
        var namespacePath = namespaceFrom(tableId);
        var tableName = tableNameFrom(tableId);
        var storageOptions = request.getStorageOptions();
        var tableProperties = request.getProperties();
        var location = String.format("%s/%s/%s", defaultLocation, namespacePath, tableName);

        transactionManager.inTransaction(tx -> {
            var maybeTable = tableRepository.get(tx, tableId);

            if (maybeTable.isPresent()) {
                switch (mode) {
                    case "create": throw new TableAlreadyExistsException(String.format("Table %s already exists", tableId));
                    case "existok", "exist_ok": return;
                }
            }

            tableRepository.upsert(
                    tx,
                    tableId,
                    namespacePath,
                    tableName,
                    location,
                    mapOrEmpty(request.getProperties())
            );
        });

        return new CreateTableResponse()
                .location(location)
                .properties(tableProperties)
                .storageOptions(storageOptions);
    }

    @Override
    public RestoreTableResponse restoreTable(RestoreTableRequest request) {
        var tableId = fullObjectName(request.getId());
        var location = transactionManager.inTransactionR(tx -> {
            var maybeTable = tableRepository.get(tx, tableId);
            if (maybeTable.isEmpty()) {
                throw new TableNotFoundException(String.format("Table metadata %s not found", tableId));
            }

            return maybeTable.get().location();
        });

        try (var dataset = open(allocator, location)) {
            dataset.checkoutVersion(request.getVersion());
            dataset.restore();
            return new RestoreTableResponse();
        }
    }

    @Override
    public RenameTableResponse renameTable(RenameTableRequest request) {
        var from = fullObjectName(request.getId());
        var to = "";

        if (request.getNewNamespaceId() == null || request.getNewNamespaceId().isEmpty()) {
            // table stay in the same namespace
            var namespace = namespaceFrom(from);
            to = fullObjectName(List.of(namespace, to));
        } else {
            // table should also be moved to the another namespace
            var ids = new ArrayList<>(request.getNewNamespaceId());
            ids.add(request.getNewTableName());

            to = fullObjectName(ids);
        }

        var fromId = from;
        var toId = to;

        transactionManager.inTransaction(tx -> {
            var maybeFromTable = tableRepository.get(tx, fromId);
            var isNewTableExist = tableRepository.exists(tx, toId);

            if (isNewTableExist) {
                throw new TableAlreadyExistsException(String.format("Table %s already exists", toId));
            }

            if (maybeFromTable.isEmpty()) {
                throw new TableNotFoundException(String.format("Table %s not found", fromId));
            }

            var table = maybeFromTable.get();
            // rename through deletion
            tableRepository.delete(tx, fromId);
            tableRepository.upsert(
                    tx,
                    toId,
                    namespaceFrom(toId),
                    tableNameFrom(toId),
                    table.location(),
                    table.properties()
            );
        });

        return new RenameTableResponse();
    }

    @Override
    public DeclareTableResponse declareTable(DeclareTableRequest request) {
        var tableId = fullObjectName(request.getId());
        var namespacePath = namespaceFrom(tableId);
        var tableName = tableNameFrom(tableId);

        transactionManager.inTransaction(tx -> {
            var isNamespaceExist = namespaceRepository.exists(tx, namespacePath);

            if (!isNamespaceExist) {
                throw new NamespaceNotFoundException(String.format("Namespace %s does not exist", namespacePath));
            }

            var isTableExist = tableRepository.exists(tx, tableId);

            if (isTableExist) {
                throw new TableAlreadyExistsException(String.format("Table %s already exist", tableId));
            }

            tableRepository.upsert(
                    tx,
                    tableId,
                    namespacePath,
                    tableName,
                    request.getLocation(),
                    mapOrEmpty(request.getProperties())
            );
        });

        return new DeclareTableResponse()
                .location(request.getLocation())
                .properties(request.getProperties())
                .managedVersioning(true);
    }

    @Override
    public AlterTableAlterColumnsResponse alterTableAlterColumns(AlterTableAlterColumnsRequest request) {
        var tableId = fullObjectName(request.getId());
        var location = transactionManager.inTransactionR(tx -> {
            var maybeTable = tableRepository.get(tx, tableId);
            if (maybeTable.isEmpty()) {
                throw new TableNotFoundException(String.format("Table metadata %s not found", tableId));
            }

            return maybeTable.get().location();
        });

        try (var dataset = open(allocator, location)) {
            var alterations = new ArrayList<ColumnAlteration>();

            for (var entry : request.getAlterations()) {
                var builder = new ColumnAlteration.Builder(entry.getPath());
                if (entry.getRename() != null) {
                    builder.rename(entry.getRename());
                }
                if (entry.getNullable() != null) {
                    builder.nullable(entry.getNullable());
                }
                alterations.add(builder.build());
            }

            dataset.alterColumns(alterations);
            return new AlterTableAlterColumnsResponse().version(dataset.version());
        }
    }

    @Override
    public AlterTableDropColumnsResponse alterTableDropColumns(AlterTableDropColumnsRequest request) {
        var tableId = fullObjectName(request.getId());
        var location = transactionManager.inTransactionR(tx -> {
            var maybeTable = tableRepository.get(tx, tableId);
            if (maybeTable.isEmpty()) {
                throw new TableNotFoundException(String.format("Table metadata %s not found", tableId));
            }

            return maybeTable.get().location();
        });

        try (var dataset = open(allocator, location)) {
            dataset.dropColumns(request.getColumns());
            return new AlterTableDropColumnsResponse().version(dataset.version());
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
        if (allocator != null) {
            allocator.close();
        }
    }

    private String fullObjectName(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return String.join(".", ids);
    }

    private String namespaceFrom(String tableId) {
        var idx = tableId.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return tableId.substring(0, idx);
    }

    private String tableNameFrom(String tableId) {
        var idx = tableId.lastIndexOf('.');
        if (idx < 0) {
            return tableId;
        }
        return tableId.substring(idx + 1);
    }

    private TableMetadata deleteTableMetadata(String tableId) {
        return transactionManager.inTransactionR(tx -> {
            var maybeTable = tableRepository.get(tx, tableId);

            if (maybeTable.isEmpty()) {
                throw new TableNotFoundException(String.format("Table %s not found", tableId));
            }

            var deleted = tableRepository.delete(tx, tableId);

            if (deleted) {
                return maybeTable.get();
            }

            throw new RuntimeException("Couldn't delete table metadata");
        });
    }

    private void purgeTableData(String location, Map<String, String> properties) {
        Dataset.drop(location, properties);
    }

    private Dataset open(BufferAllocator allocator, String location) {
        return Dataset.open().allocator(allocator).uri(location).build();
    }
}
