package kasanari.catalog.lance;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.lance.NamespaceRepository;
import kasanari.repository.lance.TableRepository;
import kasanari.repository.lance.postgres.JdbcNamespaceRepository;
import kasanari.repository.lance.postgres.JdbcTableInitializer;
import kasanari.repository.lance.postgres.JdbcTableRepository;
import org.apache.arrow.memory.BufferAllocator;
import org.jdbi.v3.core.Handle;
import org.lance.namespace.LanceNamespace;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KasanariLanceCatalog implements LanceNamespace, AutoCloseable {
    private KasanariDataSource dataSource;
    private BufferAllocator allocator;
    private NamespaceRepository<Handle> namespaceRepository;
    private TableRepository<Handle> tableRepository;
    private TransactionManager<Handle> transactionManager;

    @Override
    public void initialize(Map<String, String> properties, BufferAllocator allocator) {
        this.allocator = allocator;
        this.dataSource = new KasanariDataSource(properties);
        this.transactionManager = new JdbcTransactionManager(dataSource);
        new JdbcTableInitializer(transactionManager).initialize();

        this.namespaceRepository = new JdbcNamespaceRepository();
        this.tableRepository = new JdbcTableRepository();
    }

    @Override
    public String namespaceId() {
        return "kasanari-lance";
    }

    @Override
    public CreateNamespaceResponse createNamespace(CreateNamespaceRequest request) {
        var namespacePath = joinIds(request.getId());
        transactionManager.inTransaction(tx -> namespaceRepository.upsert(tx, namespacePath, mapOrEmpty(request.getProperties())));
        return new CreateNamespaceResponse().properties(mapOrEmpty(request.getProperties()));
    }

    @Override
    public DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request) {
        var namespacePath = joinIds(request.getId());
        var properties = transactionManager.inTransactionR(tx -> namespaceRepository.properties(tx, namespacePath));
        return new DescribeNamespaceResponse().properties(properties);
    }

    @Override
    public DropNamespaceResponse dropNamespace(DropNamespaceRequest request) {
        var namespacePath = joinIds(request.getId());
        transactionManager.inTransaction(tx -> namespaceRepository.delete(tx, namespacePath));
        return new DropNamespaceResponse();
    }

    @Override
    public void namespaceExists(NamespaceExistsRequest request) {
        var namespacePath = joinIds(request.getId());
        var exists = transactionManager.inTransactionR(tx -> namespaceRepository.exists(tx, namespacePath));
        if (!exists) {
            throw new IllegalStateException("Namespace does not exist: " + namespacePath);
        }
    }

    @Override
    public ListNamespacesResponse listNamespaces(ListNamespacesRequest request) {
        var parent = joinIds(request.getId());
        var children = transactionManager.inTransactionR(tx -> namespaceRepository.list(tx, parent));
        var response = new ListNamespacesResponse();
        response.setNamespaces(new java.util.LinkedHashSet<>(children));
        response.setPageToken(null);
        return response;
    }

    @Override
    public ListTablesResponse listTables(ListTablesRequest request) {
        var namespacePath = joinIds(request.getId());
        var tables = transactionManager.inTransactionR(tx -> tableRepository.listByNamespace(tx, namespacePath));
        var response = new ListTablesResponse();
        response.setTables(new java.util.LinkedHashSet<>(tables));
        response.setPageToken(null);
        return response;
    }

    @Override
    public RegisterTableResponse registerTable(RegisterTableRequest request) {
        var tableId = joinIds(request.getId());
        var namespacePath = namespaceFrom(tableId);
        var tableName = tableNameFrom(tableId);

        transactionManager.inTransaction(tx -> tableRepository.upsert(
                tx,
                tableId,
                namespacePath,
                tableName,
                request.getLocation(),
                mapOrEmpty(request.getProperties()),
                false
        ));
        return new RegisterTableResponse()
                .location(request.getLocation())
                .properties(mapOrEmpty(request.getProperties()));
    }

    @Override
    public DescribeTableResponse describeTable(DescribeTableRequest request) {
        var tableId = joinIds(request.getId());
        return transactionManager.inTransactionR(tx -> {
            var table = tableRepository.get(tx, tableId);
            if (table == null) {
                throw new IllegalStateException("Table does not exist: " + tableId);
            }

            return new DescribeTableResponse()
                    .table(table.tableName())
                    .namespace(splitNamespace(table.namespacePath()))
                    .location(table.location())
                    .properties(table.properties() != null ? new HashMap<>(table.properties()) : new HashMap<>())
                    .isOnlyDeclared(table.declaredOnly());
        });
    }

    @Override
    public void tableExists(TableExistsRequest request) {
        var tableId = joinIds(request.getId());
        var exists = transactionManager.inTransactionR(tx -> tableRepository.exists(tx, tableId));
        if (!exists) {
            throw new IllegalStateException("Table does not exist: " + tableId);
        }
    }

    @Override
    public DropTableResponse dropTable(DropTableRequest request) {
        var tableId = joinIds(request.getId());
        return transactionManager.inTransactionR(tx -> {
            var table = tableRepository.get(tx, tableId);
            tableRepository.delete(tx, tableId);
            return new DropTableResponse()
                    .id(request.getId())
                    .location(table == null ? null : table.location())
                    .properties(table == null || table.properties() == null ? new HashMap<>() : new HashMap<>(table.properties()));
        });
    }

    @Override
    public DeregisterTableResponse deregisterTable(DeregisterTableRequest request) {
        var tableId = joinIds(request.getId());
        return transactionManager.inTransactionR(tx -> {
            var table = tableRepository.get(tx, tableId);
            tableRepository.delete(tx, tableId);
            return new DeregisterTableResponse()
                    .id(request.getId())
                    .location(table == null ? null : table.location())
                    .properties(table == null || table.properties() == null ? new HashMap<>() : new HashMap<>(table.properties()));
        });
    }

    @Override
    public CreateTableResponse createTable(CreateTableRequest request, byte[] requestData) {
        var created = LanceNamespace.super.createTable(request, requestData);
        var tableId = joinIds(request.getId());
        var namespacePath = namespaceFrom(tableId);
        var tableName = tableNameFrom(tableId);
        var location = created.getLocation();

        transactionManager.inTransaction(tx -> tableRepository.upsert(
                tx,
                tableId,
                namespacePath,
                tableName,
                location,
                mapOrEmpty(request.getProperties()),
                false
        ));
        return created;
    }

    @Override
    public RestoreTableResponse restoreTable(RestoreTableRequest request) {
        var location = requireTableLocation(request.getId());
        return LanceDatasetSupport.restoreTable(allocator, location, request);
    }

    @Override
    public RenameTableResponse renameTable(RenameTableRequest request) {
        var sourceId = joinIds(request.getId());
        var newId = new ArrayList<>(request.getNewNamespaceId() != null && !request.getNewNamespaceId().isEmpty()
                ? request.getNewNamespaceId()
                : request.getId());
        newId.set(newId.size() - 1, request.getNewTableName());
        var destinationId = joinIds(newId);
        return transactionManager.inTransactionR(tx -> {
            var table = tableRepository.get(tx, sourceId);
            if (table == null) {
                throw new IllegalStateException("Table does not exist: " + sourceId);
            }

            tableRepository.delete(tx, sourceId);
            tableRepository.upsert(
                    tx,
                    destinationId,
                    namespaceFrom(destinationId),
                    tableNameFrom(destinationId),
                    table.location(),
                    table.properties(),
                    table.declaredOnly()
            );

            return new RenameTableResponse();
        });
    }

    @Override
    public DeclareTableResponse declareTable(DeclareTableRequest request) {
        var tableId = joinIds(request.getId());
        var namespacePath = namespaceFrom(tableId);
        var tableName = tableNameFrom(tableId);

        transactionManager.inTransaction(tx -> tableRepository.upsert(
                tx,
                tableId,
                namespacePath,
                tableName,
                request.getLocation(),
                mapOrEmpty(request.getProperties()),
                true
        ));
        return new DeclareTableResponse()
                .location(request.getLocation())
                .properties(mapOrEmpty(request.getProperties()))
                .managedVersioning(true);
    }

    @Override
    public AlterTableAlterColumnsResponse alterTableAlterColumns(AlterTableAlterColumnsRequest request) {
        var location = requireTableLocation(request.getId());
        return LanceDatasetSupport.alterTableAlterColumns(allocator, location, request);
    }

    @Override
    public AlterTableDropColumnsResponse alterTableDropColumns(AlterTableDropColumnsRequest request) {
        var location = requireTableLocation(request.getId());
        return LanceDatasetSupport.alterTableDropColumns(allocator, location, request);
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

    private String requireTableLocation(List<String> tableId) {
        var joined = joinIds(tableId);
        var location = transactionManager.inTransactionR(tx -> {
            var table = tableRepository.get(tx, joined);
            return table == null ? null : table.location();
        });
        if (location == null || location.isBlank()) {
            throw new IllegalStateException("Table does not exist or has no location: " + joined);
        }
        return location;
    }

    private Map<String, String> mapOrEmpty(Map<String, String> input) {
        if (input == null) {
            return Map.of();
        }

        return input;
    }

    private String joinIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return String.join(".", ids);
    }

    private static String namespaceFrom(String tableId) {
        var idx = tableId.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return tableId.substring(0, idx);
    }

    private static String tableNameFrom(String tableId) {
        var idx = tableId.lastIndexOf('.');
        if (idx < 0) {
            return tableId;
        }
        return tableId.substring(idx + 1);
    }

    private static java.util.List<String> splitNamespace(String namespacePath) {
        if (namespacePath == null || namespacePath.isBlank()) {
            return new ArrayList<>();
        }
        return java.util.List.of(namespacePath.split("[.]"));
    }
}
