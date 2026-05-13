package kasanari.catalog.lance;

import kasanari.catalog.lance.jdbc.JdbcTableInitializer;
import kasanari.catalog.lance.jdbc.NamespaceJdbcRepository;
import kasanari.catalog.lance.jdbc.TableJdbcRepository;
import kasanari.catalog.lance.jdbc.TableVersionJdbcRepository;
import kasanari.catalog.lance.jdbc.TransactionJdbcRepository;
import kasanari.repository.jdbc.KasanariDataSource;
import org.apache.arrow.memory.BufferAllocator;
import org.lance.namespace.LanceNamespace;
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
import org.lance.namespace.model.CreateTableVersionEntry;
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
import java.util.Map;
import java.util.UUID;

public class KasanariLanceCatalog implements LanceNamespace, AutoCloseable {
    private KasanariDataSource dataSource;
    private BufferAllocator allocator;
    private NamespaceJdbcRepository namespaceRepository;
    private TableJdbcRepository tableRepository;
    private TableVersionJdbcRepository tableVersionRepository;
    private TransactionJdbcRepository transactionRepository;

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

        this.namespaceRepository = new NamespaceJdbcRepository(dataSource);
        this.tableRepository = new TableJdbcRepository(dataSource);
        this.tableVersionRepository = new TableVersionJdbcRepository(dataSource);
        this.transactionRepository = new TransactionJdbcRepository(dataSource);
    }

    @Override
    public String namespaceId() {
        return "kasanari-jdbc";
    }

    @Override
    public CreateNamespaceResponse createNamespace(CreateNamespaceRequest request) {
        var namespacePath = namespaceIdFrom(request.getId());
        namespaceRepository.upsert(namespacePath, mapOrEmpty(request.getProperties()));
        return new CreateNamespaceResponse().properties(mapOrEmpty(request.getProperties()));
    }

    @Override
    public DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request) {
        var namespacePath = namespaceIdFrom(request.getId());
        var properties = namespaceRepository.properties(namespacePath);
        return new DescribeNamespaceResponse().properties(properties);
    }

    @Override
    public DropNamespaceResponse dropNamespace(DropNamespaceRequest request) {
        var namespacePath = namespaceIdFrom(request.getId());
        namespaceRepository.delete(namespacePath);
        return new DropNamespaceResponse();
    }

    @Override
    public void namespaceExists(NamespaceExistsRequest request) {
        var namespacePath = namespaceIdFrom(request.getId());
        if (!namespaceRepository.exists(namespacePath)) {
            throw new IllegalStateException("Namespace does not exist: " + namespacePath);
        }
    }

    @Override
    public ListNamespacesResponse listNamespaces(ListNamespacesRequest request) {
        var parent = namespaceIdFrom(request.getId());
        var children = namespaceRepository.list(parent);
        var response = new ListNamespacesResponse();
        response.setNamespaces(new java.util.LinkedHashSet<>(children));
        response.setPageToken(null);
        return response;
    }

    @Override
    public ListTablesResponse listTables(ListTablesRequest request) {
        var namespacePath = namespaceIdFrom(request.getId());
        var tables = tableRepository.listByNamespace(namespacePath);
        var response = new ListTablesResponse();
        response.setTables(new java.util.LinkedHashSet<>(tables));
        response.setPageToken(null);
        return response;
    }

    @Override
    public RegisterTableResponse registerTable(RegisterTableRequest request) {
        var tableId = tableIdFrom(request.getId());
        var namespacePath = namespaceFrom(tableId);
        var tableName = tableNameFrom(tableId);

        tableRepository.upsert(tableId, namespacePath, tableName, request.getLocation(), mapOrEmpty(request.getProperties()), false);
        return new RegisterTableResponse()
                .location(request.getLocation())
                .properties(mapOrEmpty(request.getProperties()));
    }

    @Override
    public DescribeTableResponse describeTable(DescribeTableRequest request) {
        var tableId = tableIdFrom(request.getId());
        var table = tableRepository.get(tableId);
        if (table == null) {
            throw new IllegalStateException("Table does not exist: " + tableId);
        }

        var response = new DescribeTableResponse()
                .table(table.tableName())
                .namespace(splitNamespace(table.namespacePath()))
                .location(table.location())
                .properties(table.propertiesOrEmpty())
                .isOnlyDeclared(table.declaredOnly());

        var latest = tableVersionRepository.list(tableId, true, 1, null);
        if (!latest.isEmpty()) {
            response.version(latest.get(0).getVersion());
        }
        return response;
    }

    @Override
    public void tableExists(TableExistsRequest request) {
        var tableId = tableIdFrom(request.getId());
        if (!tableRepository.exists(tableId)) {
            throw new IllegalStateException("Table does not exist: " + tableId);
        }
    }

    @Override
    public DropTableResponse dropTable(DropTableRequest request) {
        var tableId = tableIdFrom(request.getId());
        var table = tableRepository.get(tableId);
        tableVersionRepository.deleteForTable(tableId);
        tableRepository.delete(tableId);
        return new DropTableResponse()
                .id(request.getId())
                .location(table == null ? null : table.location())
                .properties(table == null ? new HashMap<>() : table.propertiesOrEmpty());
    }

    @Override
    public DeregisterTableResponse deregisterTable(DeregisterTableRequest request) {
        var tableId = tableIdFrom(request.getId());
        var table = tableRepository.get(tableId);
        tableVersionRepository.deleteForTable(tableId);
        tableRepository.delete(tableId);
        return new DeregisterTableResponse()
                .id(request.getId())
                .location(table == null ? null : table.location())
                .properties(table == null ? new HashMap<>() : table.propertiesOrEmpty());
    }

    @Override
    public ListTableVersionsResponse listTableVersions(ListTableVersionsRequest request) {
        var tableId = tableIdFrom(request.getId());
        var versions = tableVersionRepository.list(
                tableId,
                Boolean.TRUE.equals(request.getDescending()),
                request.getLimit(),
                request.getPageToken()
        );
        return new ListTableVersionsResponse().versions(versions);
    }

    @Override
    public CreateTableVersionResponse createTableVersion(CreateTableVersionRequest request) {
        var tableId = tableIdFrom(request.getId());
        var row = new TableVersion()
                .version(request.getVersion())
                .manifestPath(request.getManifestPath())
                .manifestSize(request.getManifestSize())
                .eTag(request.geteTag())
                .metadata(mapOrEmpty(request.getMetadata()))
                .timestampMillis(System.currentTimeMillis());
        tableVersionRepository.create(tableId, row);
        return new CreateTableVersionResponse().version(row);
    }

    @Override
    public DescribeTableVersionResponse describeTableVersion(DescribeTableVersionRequest request) {
        var tableId = tableIdFrom(request.getId());
        var row = tableVersionRepository.get(tableId, request.getVersion());
        if (row == null) {
            throw new IllegalStateException("Table version does not exist");
        }
        return new DescribeTableVersionResponse().version(row);
    }

    @Override
    public BatchDeleteTableVersionsResponse batchDeleteTableVersions(BatchDeleteTableVersionsRequest request) {
        var tableId = tableIdFrom(request.getId());
        var ranges = new ArrayList<VersionRange>();
        for (var value : request.getRanges()) {
            ranges.add((VersionRange) value);
        }
        var deleted = tableVersionRepository.deleteRanges(tableId, ranges);
        return new BatchDeleteTableVersionsResponse().deletedCount(deleted);
    }

    @Override
    public BatchCreateTableVersionsResponse batchCreateTableVersions(BatchCreateTableVersionsRequest request) {
        var created = new ArrayList<TableVersion>();
        for (var value : request.getEntries()) {
            var entry = (CreateTableVersionEntry) value;
            var tableId = tableIdFrom(entry.getId());
            var row = new TableVersion()
                    .version(entry.getVersion())
                    .manifestPath(entry.getManifestPath())
                    .manifestSize(entry.getManifestSize())
                    .eTag(entry.geteTag())
                    .metadata(mapOrEmpty(entry.getMetadata()))
                    .timestampMillis(System.currentTimeMillis());
            tableVersionRepository.create(tableId, row);
            created.add(row);
        }
        return new BatchCreateTableVersionsResponse().versions(created);
    }

    @Override
    public BatchCommitTablesResponse batchCommitTables(BatchCommitTablesRequest request) {
        var transactionId = UUID.randomUUID().toString();
        var results = new ArrayList<CommitTableResult>();
        for (var value : request.getOperations()) {
            var operation = (org.lance.namespace.model.CommitTableOperation) value;
            var result = new CommitTableResult();

            if (operation.getDeclareTable() != null) {
                var declare = operation.getDeclareTable();
                var tableId = tableIdFrom(declare.getId());
                var namespace = namespaceFrom(tableId);
                var name = tableNameFrom(tableId);
                tableRepository.upsert(tableId, namespace, name, declare.getLocation(), mapOrEmpty(declare.getProperties()), true);
                result.setDeclareTable(new org.lance.namespace.model.DeclareTableResponse()
                        .location(declare.getLocation())
                        .properties(mapOrEmpty(declare.getProperties()))
                        .managedVersioning(true)
                        .transactionId(transactionId));
            }

            if (operation.getCreateTableVersion() != null) {
                var created = createTableVersion(operation.getCreateTableVersion());
                created.setTransactionId(transactionId);
                result.setCreateTableVersion(created);
            }

            if (operation.getDeleteTableVersions() != null) {
                var deleted = batchDeleteTableVersions(operation.getDeleteTableVersions());
                deleted.setTransactionId(transactionId);
                result.setDeleteTableVersions(deleted);
            }

            if (operation.getDeregisterTable() != null) {
                var deregister = deregisterTable(operation.getDeregisterTable());
                deregister.setTransactionId(transactionId);
                result.setDeregisterTable(deregister);
            }

            results.add(result);
        }

        transactionRepository.upsert(transactionId, TransactionJdbcRepository.STATUS_SUCCEEDED, Map.of("operation_count", String.valueOf(results.size())));
        return new BatchCommitTablesResponse().transactionId(transactionId).results(results);
    }

    @Override
    public DescribeTransactionResponse describeTransaction(DescribeTransactionRequest request) {
        var transactionId = transactionIdFrom(request.getId());
        var row = transactionRepository.get(transactionId);
        if (row == null) {
            return new DescribeTransactionResponse().status(TransactionJdbcRepository.STATUS_QUEUED).properties(new HashMap<>());
        }
        return new DescribeTransactionResponse().status(row.status()).properties(row.propertiesOrEmpty());
    }

    @Override
    public AlterTransactionResponse alterTransaction(AlterTransactionRequest request) {
        var transactionId = transactionIdFrom(request.getId());
        var current = transactionRepository.get(transactionId);

        var status = current == null ? TransactionJdbcRepository.STATUS_QUEUED : current.status();
        var properties = current == null ? new HashMap<String, String>() : new HashMap<>(current.propertiesOrEmpty());

        for (var value : request.getActions()) {
            var action = (org.lance.namespace.model.AlterTransactionAction) value;
            if (action.getSetStatusAction() != null && action.getSetStatusAction().getStatus() != null) {
                status = action.getSetStatusAction().getStatus();
            }
            if (action.getSetPropertyAction() != null && action.getSetPropertyAction().getKey() != null) {
                var key = action.getSetPropertyAction().getKey();
                var mode = valueOrDefault(action.getSetPropertyAction().getMode(), "Overwrite");
                if ("Fail".equalsIgnoreCase(mode) && properties.containsKey(key)) {
                    throw new IllegalStateException("Transaction property already exists: " + key);
                }
                if ("Skip".equalsIgnoreCase(mode) && properties.containsKey(key)) {
                    continue;
                }
                properties.put(key, action.getSetPropertyAction().getValue());
            }
            if (action.getUnsetPropertyAction() != null && action.getUnsetPropertyAction().getKey() != null) {
                var key = action.getUnsetPropertyAction().getKey();
                var mode = valueOrDefault(action.getUnsetPropertyAction().getMode(), "Skip");
                if ("Fail".equalsIgnoreCase(mode) && !properties.containsKey(key)) {
                    throw new IllegalStateException("Transaction property does not exist: " + key);
                }
                properties.remove(key);
            }
        }

        transactionRepository.upsert(transactionId, status, properties);
        return new AlterTransactionResponse().status(status).properties(properties);
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

    private static String valueOrDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static Map<String, String> mapOrEmpty(Map input) {
        var result = new HashMap<String, String>();
        if (input == null) {
            return result;
        }
        for (var entry : ((Map<?, ?>) input).entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }

    private static String namespaceIdFrom(java.util.List id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        return String.join(".", id);
    }

    private static String tableIdFrom(java.util.List id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Table identifier is required");
        }
        return String.join(".", id);
    }

    private static String transactionIdFrom(java.util.List id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Transaction identifier is required");
        }
        return String.join(".", id);
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
