package kasanari.catalog.lance;

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
    public CreateTableResponse createTable(CreateTableRequest request, byte[] requestData) {
        return namespace.createTable(request, requestData);
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
    public RestoreTableResponse restoreTable(RestoreTableRequest request) {
        return namespace.restoreTable(request);
    }

    @Override
    public RenameTableResponse renameTable(RenameTableRequest request) {
        return namespace.renameTable(request);
    }

    @Override
    public DeclareTableResponse createEmptyTable(DeclareTableRequest request) {
        return namespace.declareTable(request);
    }

    @Override
    public AlterTableAlterColumnsResponse alterTableAlterColumns(AlterTableAlterColumnsRequest request) {
        return namespace.alterTableAlterColumns(request);
    }

    @Override
    public AlterTableDropColumnsResponse alterTableDropColumns(AlterTableDropColumnsRequest request) {
        return namespace.alterTableDropColumns(request);
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
