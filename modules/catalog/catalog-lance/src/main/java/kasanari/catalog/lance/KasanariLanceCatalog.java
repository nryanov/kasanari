package kasanari.catalog.lance;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.lance.NamespaceRepository;
import kasanari.repository.lance.TableRepository;
import kasanari.repository.lance.TableVersionRepository;
import kasanari.repository.lance.TransactionRepository;
import kasanari.repository.lance.postgres.JdbcTableInitializer;
import kasanari.repository.lance.postgres.JdbcNamespaceRepository;
import kasanari.repository.lance.postgres.JdbcTableRepository;
import kasanari.repository.lance.postgres.JdbcTableVersionRepository;
import kasanari.repository.lance.postgres.JdbcTransactionRepository;
import kasanari.repository.jdbc.KasanariDataSource;
import org.apache.arrow.memory.BufferAllocator;
import org.jdbi.v3.core.Handle;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.model.AlterTableAddColumnsRequest;
import org.lance.namespace.model.AlterTableAddColumnsResponse;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableAlterColumnsResponse;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsResponse;
import org.lance.namespace.model.AlterTransactionRequest;
import org.lance.namespace.model.AlterTransactionResponse;
import org.lance.namespace.model.BatchCommitTablesRequest;
import org.lance.namespace.model.BatchCommitTablesResponse;
import org.lance.namespace.model.BatchCreateTableVersionsRequest;
import org.lance.namespace.model.BatchCreateTableVersionsResponse;
import org.lance.namespace.model.BatchDeleteTableVersionsRequest;
import org.lance.namespace.model.BatchDeleteTableVersionsResponse;
import org.lance.namespace.model.CommitTableResult;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateNamespaceResponse;
import org.lance.namespace.model.CreateTableRequest;
import org.lance.namespace.model.CreateTableResponse;
import org.lance.namespace.model.CreateTableVersionRequest;
import org.lance.namespace.model.CreateTableVersionResponse;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DeregisterTableResponse;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceResponse;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DescribeTableResponse;
import org.lance.namespace.model.DescribeTableVersionRequest;
import org.lance.namespace.model.DescribeTableVersionResponse;
import org.lance.namespace.model.DescribeTransactionRequest;
import org.lance.namespace.model.DescribeTransactionResponse;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.DropNamespaceResponse;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.DropTableResponse;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.ListTableVersionsRequest;
import org.lance.namespace.model.ListTableVersionsResponse;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RegisterTableResponse;
import org.lance.namespace.model.TableExistsRequest;
import org.lance.namespace.model.TableVersion;
import org.lance.namespace.model.VersionRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KasanariLanceCatalog implements LanceNamespace, AutoCloseable {
    private KasanariDataSource dataSource;
    private BufferAllocator allocator;
    private NamespaceRepository<Handle> namespaceRepository;
    private TableRepository<Handle> tableRepository;
    private TableVersionRepository<Handle> tableVersionRepository;
    private TransactionRepository<Handle> transactionRepository;
    private TransactionManager<Handle> transactionManager;

    @Override
    public void initialize(Map configProperties, BufferAllocator allocator) {
        var properties = new HashMap<String, String>();
        if (configProperties != null) {
            for (var entry : ((Map<?, ?>) configProperties).entrySet()) {
                properties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }

        this.allocator = allocator;
        this.dataSource = new KasanariDataSource(properties);
        new JdbcTableInitializer(dataSource).initialize();

        this.transactionManager = new JdbcTransactionManager(dataSource);
        this.namespaceRepository = new JdbcNamespaceRepository();
        this.tableRepository = new JdbcTableRepository();
        this.tableVersionRepository = new JdbcTableVersionRepository();
        this.transactionRepository = new JdbcTransactionRepository();
    }

    @Override
    public String namespaceId() {
        return "kasanari-jdbc";
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

        transactionManager.inTransaction(tx -> tableRepository.upsert(tx, tableId, namespacePath, tableName, request.getLocation(), mapOrEmpty(request.getProperties()), false));
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

            var response = new DescribeTableResponse()
                    .table(table.tableName())
                    .namespace(splitNamespace(table.namespacePath()))
                    .location(table.location())
                    .properties(table.properties() != null ? new HashMap<>(table.properties()) : new HashMap<>())
                    .isOnlyDeclared(table.declaredOnly());

            var latest = tableVersionRepository.list(tx, tableId, true, 1, null);
            if (!latest.isEmpty()) {
                response.version(latest.getFirst().getVersion());
            }
            return response;
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
            tableVersionRepository.deleteForTable(tx, tableId);
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
            tableVersionRepository.deleteForTable(tx, tableId);
            tableRepository.delete(tx, tableId);
            return new DeregisterTableResponse()
                    .id(request.getId())
                    .location(table == null ? null : table.location())
                    .properties(table == null || table.properties() == null ? new HashMap<>() : new HashMap<>(table.properties()));
        });
    }

    @Override
    public ListTableVersionsResponse listTableVersions(ListTableVersionsRequest request) {
        var tableId = joinIds(request.getId());
        var versions = transactionManager.inTransactionR(tx -> tableVersionRepository.list(
                tx,
                tableId,
                Boolean.TRUE.equals(request.getDescending()),
                request.getLimit(),
                request.getPageToken()
        ));
        return new ListTableVersionsResponse().versions(versions);
    }

    @Override
    public CreateTableVersionResponse createTableVersion(CreateTableVersionRequest request) {
        var tableId = joinIds(request.getId());
        var row = new TableVersion()
                .version(request.getVersion())
                .manifestPath(request.getManifestPath())
                .manifestSize(request.getManifestSize())
                .eTag(request.geteTag())
                .metadata(mapOrEmpty(request.getMetadata()))
                .timestampMillis(System.currentTimeMillis());
        transactionManager.inTransaction(tx -> tableVersionRepository.create(tx, tableId, row));
        return new CreateTableVersionResponse().version(row);
    }

    @Override
    public DescribeTableVersionResponse describeTableVersion(DescribeTableVersionRequest request) {
        var tableId = joinIds(request.getId());
        var row = transactionManager.inTransactionR(tx -> tableVersionRepository.get(tx, tableId, request.getVersion()));
        if (row == null) {
            throw new IllegalStateException("Table version does not exist");
        }
        return new DescribeTableVersionResponse().version(row);
    }

    @Override
    public BatchDeleteTableVersionsResponse batchDeleteTableVersions(BatchDeleteTableVersionsRequest request) {
        var tableId = joinIds(request.getId());
        var ranges = new ArrayList<VersionRange>();
        for (var value : request.getRanges()) {
            ranges.add(value);
        }
        var deleted = transactionManager.inTransactionR(tx -> tableVersionRepository.deleteRanges(tx, tableId, ranges));
        return new BatchDeleteTableVersionsResponse().deletedCount(deleted);
    }

    @Override
    public BatchCreateTableVersionsResponse batchCreateTableVersions(BatchCreateTableVersionsRequest request) {
        return transactionManager.inTransactionR(tx -> {
            var created = new ArrayList<TableVersion>();
            for (var value : request.getEntries()) {
                var tableId = joinIds(value.getId());
                var row = new TableVersion()
                        .version(value.getVersion())
                        .manifestPath(value.getManifestPath())
                        .manifestSize(value.getManifestSize())
                        .eTag(value.geteTag())
                        .metadata(mapOrEmpty(value.getMetadata()))
                        .timestampMillis(System.currentTimeMillis());
                tableVersionRepository.create(tx, tableId, row);
                created.add(row);
            }
            return new BatchCreateTableVersionsResponse().versions(created);
        });
    }

    @Override
    public CreateTableResponse createTable(CreateTableRequest request, byte[] requestData) {
        return LanceNamespace.super.createTable(request, requestData);
    }

    @Override
    public AlterTableAddColumnsResponse alterTableAddColumns(AlterTableAddColumnsRequest request) {
        return LanceNamespace.super.alterTableAddColumns(request);
    }

    @Override
    public AlterTableAlterColumnsResponse alterTableAlterColumns(AlterTableAlterColumnsRequest request) {
        return LanceNamespace.super.alterTableAlterColumns(request);
    }

    @Override
    public AlterTableDropColumnsResponse alterTableDropColumns(AlterTableDropColumnsRequest request) {
        return LanceNamespace.super.alterTableDropColumns(request);
    }

    @Override
    public BatchCommitTablesResponse batchCommitTables(BatchCommitTablesRequest request) {
        return transactionManager.inTransactionR(tx -> {
            var transactionId = UUID.randomUUID().toString();
            var results = new ArrayList<CommitTableResult>();
            for (var value : request.getOperations()) {
                var result = new CommitTableResult();

                if (value.getDeclareTable() != null) {
                    var declare = value.getDeclareTable();
                    var tableId = joinIds(declare.getId());
                    var namespace = namespaceFrom(tableId);
                    var name = tableNameFrom(tableId);
                    tableRepository.upsert(tx, tableId, namespace, name, declare.getLocation(), mapOrEmpty(declare.getProperties()), true);
                    result.setDeclareTable(new org.lance.namespace.model.DeclareTableResponse()
                            .location(declare.getLocation())
                            .properties(mapOrEmpty(declare.getProperties()))
                            .managedVersioning(true)
                            .transactionId(transactionId));
                }

                if (value.getCreateTableVersion() != null) {
                    var ctv = value.getCreateTableVersion();
                    var tableIdCtv = joinIds(ctv.getId());
                    var row = new TableVersion()
                            .version(ctv.getVersion())
                            .manifestPath(ctv.getManifestPath())
                            .manifestSize(ctv.getManifestSize())
                            .eTag(ctv.geteTag())
                            .metadata(mapOrEmpty(ctv.getMetadata()))
                            .timestampMillis(System.currentTimeMillis());
                    tableVersionRepository.create(tx, tableIdCtv, row);
                    var created = new CreateTableVersionResponse().version(row).transactionId(transactionId);
                    result.setCreateTableVersion(created);
                }

                if (value.getDeleteTableVersions() != null) {
                    var delReq = value.getDeleteTableVersions();
                    var tableIdDel = joinIds(delReq.getId());
                    var ranges = new ArrayList<>(delReq.getRanges());
                    var deletedCount = tableVersionRepository.deleteRanges(tx, tableIdDel, ranges);
                    var deleted = new BatchDeleteTableVersionsResponse()
                            .deletedCount(deletedCount)
                            .transactionId(transactionId);
                    result.setDeleteTableVersions(deleted);
                }

                if (value.getDeregisterTable() != null) {
                    var deregReq = value.getDeregisterTable();
                    var tableIdDr = joinIds(deregReq.getId());
                    var table = tableRepository.get(tx, tableIdDr);
                    tableVersionRepository.deleteForTable(tx, tableIdDr);
                    tableRepository.delete(tx, tableIdDr);
                    var deregister = new DeregisterTableResponse()
                            .id(deregReq.getId())
                            .location(table == null ? null : table.location())
                            .properties(table == null || table.properties() == null ? new HashMap<>() : new HashMap<>(table.properties()))
                            .transactionId(transactionId);
                    result.setDeregisterTable(deregister);
                }

                results.add(result);
            }

            transactionRepository.upsert(tx, transactionId, JdbcTransactionRepository.STATUS_SUCCEEDED, Map.of("operation_count", String.valueOf(results.size())));
            return new BatchCommitTablesResponse().transactionId(transactionId).results(results);
        });
    }

    @Override
    public DescribeTransactionResponse describeTransaction(DescribeTransactionRequest request) {
        var transactionId = joinIds(request.getId());
        var row = transactionManager.inTransactionR(tx -> transactionRepository.get(tx, transactionId));
        if (row == null) {
            return new DescribeTransactionResponse().status(JdbcTransactionRepository.STATUS_QUEUED).properties(new HashMap<>());
        }
        return new DescribeTransactionResponse().status(row.status()).properties(
                row.properties() != null ? new HashMap<>(row.properties()) : new HashMap<>());
    }

    @Override
    public AlterTransactionResponse alterTransaction(AlterTransactionRequest request) {
        var transactionId = joinIds(request.getId());
        return transactionManager.inTransactionR(tx -> {
            var current = transactionRepository.get(tx, transactionId);

            var status = current == null ? JdbcTransactionRepository.STATUS_QUEUED : current.status();
            var properties = current == null || current.properties() == null
                    ? new HashMap<String, String>()
                    : new HashMap<>(current.properties());

            for (var value : request.getActions()) {
                if (value.getSetStatusAction() != null && value.getSetStatusAction().getStatus() != null) {
                    status = value.getSetStatusAction().getStatus();
                }
                if (value.getSetPropertyAction() != null && value.getSetPropertyAction().getKey() != null) {
                    var key = value.getSetPropertyAction().getKey();
                    var mode = valueOrDefault(value.getSetPropertyAction().getMode(), "Overwrite");
                    if ("Fail".equalsIgnoreCase(mode) && properties.containsKey(key)) {
                        throw new IllegalStateException("Transaction property already exists: " + key);
                    }
                    if ("Skip".equalsIgnoreCase(mode) && properties.containsKey(key)) {
                        continue;
                    }
                    properties.put(key, value.getSetPropertyAction().getValue());
                }
                if (value.getUnsetPropertyAction() != null && value.getUnsetPropertyAction().getKey() != null) {
                    var key = value.getUnsetPropertyAction().getKey();
                    var mode = valueOrDefault(value.getUnsetPropertyAction().getMode(), "Skip");
                    if ("Fail".equalsIgnoreCase(mode) && !properties.containsKey(key)) {
                        throw new IllegalStateException("Transaction property does not exist: " + key);
                    }
                    properties.remove(key);
                }
            }

            transactionRepository.upsert(tx, transactionId, status, properties);
            return new AlterTransactionResponse().status(status).properties(properties);
        });
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

    private String valueOrDefault(String value, String fallback) {
        return value == null ? fallback : value;
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
