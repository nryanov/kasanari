package kasanari.catalog.lance;

import org.lance.namespace.LanceNamespace;
import org.lance.namespace.model.AlterTableAddColumnsRequest;
import org.lance.namespace.model.AlterTableAddColumnsResponse;
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

public interface LanceCatalogAdapter extends AutoCloseable {
    CreateNamespaceResponse createNamespace(CreateNamespaceRequest request);

    ListNamespacesResponse listNamespaces(ListNamespacesRequest request);

    DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request);

    DropNamespaceResponse dropNamespace(DropNamespaceRequest request);

    void namespaceExists(NamespaceExistsRequest request);

    ListTablesResponse listTables(ListTablesRequest request);

    CreateTableResponse createTable(CreateTableRequest request, byte[] requestData);

    DescribeTableResponse describeTable(DescribeTableRequest request);

    DropTableResponse dropTable(DropTableRequest request);

    void tableExists(TableExistsRequest request);

    RegisterTableResponse registerTable(RegisterTableRequest request);

    DeregisterTableResponse deregisterTable(DeregisterTableRequest request);

    DeclareTableResponse createEmptyTable(DeclareTableRequest request);

    RestoreTableResponse restoreTable(RestoreTableRequest request);

    RenameTableResponse renameTable(RenameTableRequest request);

    AlterTableAlterColumnsResponse alterTableAlterColumns(AlterTableAlterColumnsRequest request);

    AlterTableDropColumnsResponse alterTableDropColumns(AlterTableDropColumnsRequest request);

    AlterTableAddColumnsResponse alterTableAddColumns(AlterTableAddColumnsRequest request);

    LanceNamespace delegate();
}
