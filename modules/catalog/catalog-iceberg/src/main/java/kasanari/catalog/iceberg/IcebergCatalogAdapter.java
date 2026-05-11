package kasanari.catalog.iceberg;

import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.GetNamespaceResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.LoadTableResponse;
import org.apache.iceberg.rest.responses.LoadViewResponse;
import org.apache.iceberg.rest.responses.UpdateNamespacePropertiesResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IcebergCatalogAdapter {
    CreateNamespaceResponse createNamespace(Namespace namespace, Map<String, String> properties);

    default CreateNamespaceResponse createNamespace(Namespace namespace) {
        return createNamespace(namespace, new HashMap<>());
    }

    void dropNamespace(Namespace namespace);

    GetNamespaceResponse loadNamespaceMetadata(Namespace namespace);

    boolean namespaceExists(Namespace namespace);

    ListNamespacesResponse listNamespaces(String pageToken, Integer pageSize, String parent);

    UpdateNamespacePropertiesResponse updateNamespace(Namespace namespace, Map<String, String> updates, Set<String> removals);

    LoadViewResponse createView(Namespace namespace, CreateViewRequest rq);

    boolean viewExists(TableIdentifier view);

    LoadViewResponse loadView(TableIdentifier view);

    void renameView(TableIdentifier from, TableIdentifier to);

    ListTablesResponse listViews(Namespace namespace, String pageToken, Integer pageSize);

    void dropView(TableIdentifier view);

    LoadViewResponse replaceView(TableIdentifier view, UpdateTableRequest rq);

    boolean tableExists(TableIdentifier table);

    void dropTable(TableIdentifier table, boolean purge);

    ListTablesResponse listTables(Namespace namespace, String pageToken, Integer pageSize);

    LoadTableResponse createTable(Namespace namespace, CreateTableRequest rq);

    void renameTable(TableIdentifier from, TableIdentifier to);

    LoadTableResponse registerTable(TableIdentifier table, String location);

    LoadTableResponse updateTable(TableIdentifier table, UpdateTableRequest rq);

    LoadTableResponse loadTable(TableIdentifier table);

    void commitTransaction(List<UpdateTableRequest> transactions);

    Catalog delegate();
}
