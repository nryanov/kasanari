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

public interface LanceCatalogAdapter extends AutoCloseable {
    CreateNamespaceResponse createNamespace(CreateNamespaceRequest request);

    DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request);

    DropNamespaceResponse dropNamespace(DropNamespaceRequest request);

    void namespaceExists(NamespaceExistsRequest request);

    ListNamespacesResponse listNamespaces(ListNamespacesRequest request);

    ListTablesResponse listTables(ListTablesRequest request);

    RegisterTableResponse registerTable(RegisterTableRequest request);

    DescribeTableResponse describeTable(DescribeTableRequest request);

    void tableExists(TableExistsRequest request);

    DropTableResponse dropTable(DropTableRequest request);

    DeregisterTableResponse deregisterTable(DeregisterTableRequest request);

    ListTableVersionsResponse listTableVersions(ListTableVersionsRequest request);

    CreateTableVersionResponse createTableVersion(CreateTableVersionRequest request);

    DescribeTableVersionResponse describeTableVersion(DescribeTableVersionRequest request);

    BatchDeleteTableVersionsResponse batchDeleteTableVersions(BatchDeleteTableVersionsRequest request);

    BatchCreateTableVersionsResponse batchCreateTableVersions(BatchCreateTableVersionsRequest request);

    BatchCommitTablesResponse batchCommitTables(BatchCommitTablesRequest request);

    DescribeTransactionResponse describeTransaction(DescribeTransactionRequest request);

    AlterTransactionResponse alterTransaction(AlterTransactionRequest request);

    LanceNamespace delegate();
}
