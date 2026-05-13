package kasanari.catalog.lance;

import org.lance.namespace.LanceNamespace;
import org.lance.namespace.model.AlterTransactionRequest;
import org.lance.namespace.model.AlterTransactionResponse;
import org.lance.namespace.model.BatchCommitTablesRequest;
import org.lance.namespace.model.BatchCommitTablesResponse;
import org.lance.namespace.model.BatchCreateTableVersionsRequest;
import org.lance.namespace.model.BatchCreateTableVersionsResponse;
import org.lance.namespace.model.BatchDeleteTableVersionsRequest;
import org.lance.namespace.model.BatchDeleteTableVersionsResponse;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateNamespaceResponse;
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

public class DefaultLanceCatalogAdapter implements LanceCatalogAdapter {
    private final LanceNamespace namespace;

    public DefaultLanceCatalogAdapter(LanceNamespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public CreateNamespaceResponse createNamespace(CreateNamespaceRequest request) {
        return namespace.createNamespace(request);
    }

    @Override
    public DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request) {
        return namespace.describeNamespace(request);
    }

    @Override
    public DropNamespaceResponse dropNamespace(DropNamespaceRequest request) {
        return namespace.dropNamespace(request);
    }

    @Override
    public void namespaceExists(NamespaceExistsRequest request) {
        namespace.namespaceExists(request);
    }

    @Override
    public ListNamespacesResponse listNamespaces(ListNamespacesRequest request) {
        return namespace.listNamespaces(request);
    }

    @Override
    public ListTablesResponse listTables(ListTablesRequest request) {
        return namespace.listTables(request);
    }

    @Override
    public RegisterTableResponse registerTable(RegisterTableRequest request) {
        return namespace.registerTable(request);
    }

    @Override
    public DescribeTableResponse describeTable(DescribeTableRequest request) {
        return namespace.describeTable(request);
    }

    @Override
    public void tableExists(TableExistsRequest request) {
        namespace.tableExists(request);
    }

    @Override
    public DropTableResponse dropTable(DropTableRequest request) {
        return namespace.dropTable(request);
    }

    @Override
    public DeregisterTableResponse deregisterTable(DeregisterTableRequest request) {
        return namespace.deregisterTable(request);
    }

    @Override
    public ListTableVersionsResponse listTableVersions(ListTableVersionsRequest request) {
        return namespace.listTableVersions(request);
    }

    @Override
    public CreateTableVersionResponse createTableVersion(CreateTableVersionRequest request) {
        return namespace.createTableVersion(request);
    }

    @Override
    public DescribeTableVersionResponse describeTableVersion(DescribeTableVersionRequest request) {
        return namespace.describeTableVersion(request);
    }

    @Override
    public BatchDeleteTableVersionsResponse batchDeleteTableVersions(BatchDeleteTableVersionsRequest request) {
        return namespace.batchDeleteTableVersions(request);
    }

    @Override
    public BatchCreateTableVersionsResponse batchCreateTableVersions(BatchCreateTableVersionsRequest request) {
        return namespace.batchCreateTableVersions(request);
    }

    @Override
    public BatchCommitTablesResponse batchCommitTables(BatchCommitTablesRequest request) {
        return namespace.batchCommitTables(request);
    }

    @Override
    public DescribeTransactionResponse describeTransaction(DescribeTransactionRequest request) {
        return namespace.describeTransaction(request);
    }

    @Override
    public AlterTransactionResponse alterTransaction(AlterTransactionRequest request) {
        return namespace.alterTransaction(request);
    }

    @Override
    public LanceNamespace delegate() {
        return namespace;
    }

    @Override
    public void close() throws Exception {
        if (namespace instanceof AutoCloseable closeable) {
            closeable.close();
        }
    }
}
