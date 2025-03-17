package kasanari.server.iceberg;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.iceberg.CatalogApiApi;
import kasanari.api.iceberg.dto.*;

@ApplicationScoped
public class IcebergCatalogApi implements CatalogApiApi {
    @Override
    public Uni<Void> cancelPlanning(String prefix, String namespace, String table, String planId) {
        return null;
    }

    @Override
    public Uni<Void> commitTransaction(String prefix, IcebergCommitTransactionRequest icebergCommitTransactionRequest) {
        return null;
    }

    @Override
    public Uni<IcebergCreateNamespaceResponse> createNamespace(String prefix, IcebergCreateNamespaceRequest icebergCreateNamespaceRequest) {
        return null;
    }

    @Override
    public Uni<IcebergLoadTableResult> createTable(String prefix, String namespace, IcebergCreateTableRequest icebergCreateTableRequest, String xIcebergAccessDelegation) {
        return null;
    }

    @Override
    public Uni<IcebergLoadViewResult> createView(String prefix, String namespace, IcebergCreateViewRequest icebergCreateViewRequest) {
        return null;
    }

    @Override
    public Uni<Void> dropNamespace(String prefix, String namespace) {
        return null;
    }

    @Override
    public Uni<Void> dropTable(String prefix, String namespace, String table, Boolean purgeRequested) {
        return null;
    }

    @Override
    public Uni<Void> dropView(String prefix, String namespace, String view) {
        return null;
    }

    @Override
    public Uni<IcebergFetchPlanningResult> fetchPlanningResult(String prefix, String namespace, String table, String planId) {
        return null;
    }

    @Override
    public Uni<IcebergFetchScanTasksResult> fetchScanTasks(String prefix, String namespace, String table, IcebergFetchScanTasksRequest icebergFetchScanTasksRequest) {
        return null;
    }

    @Override
    public Uni<IcebergListNamespacesResponse> listNamespaces(String prefix, String pageToken, Integer pageSize, String parent) {
        return null;
    }

    @Override
    public Uni<IcebergListTablesResponse> listTables(String prefix, String namespace, String pageToken, Integer pageSize) {
        return null;
    }

    @Override
    public Uni<IcebergListTablesResponse> listViews(String prefix, String namespace, String pageToken, Integer pageSize) {
        return null;
    }

    @Override
    public Uni<IcebergLoadCredentialsResponse> loadCredentials(String prefix, String namespace, String table) {
        return null;
    }

    @Override
    public Uni<IcebergGetNamespaceResponse> loadNamespaceMetadata(String prefix, String namespace) {
        return null;
    }

    @Override
    public Uni<IcebergLoadTableResult> loadTable(String prefix, String namespace, String table, String xIcebergAccessDelegation, String ifNoneMatch, String snapshots) {
        return null;
    }

    @Override
    public Uni<IcebergLoadViewResult> loadView(String prefix, String namespace, String view) {
        return null;
    }

    @Override
    public Uni<Void> namespaceExists(String prefix, String namespace) {
        return null;
    }

    @Override
    public Uni<IcebergPlanTableScanResult> planTableScan(String prefix, String namespace, String table, IcebergPlanTableScanRequest icebergPlanTableScanRequest) {
        return null;
    }

    @Override
    public Uni<IcebergLoadTableResult> registerTable(String prefix, String namespace, IcebergRegisterTableRequest icebergRegisterTableRequest) {
        return null;
    }

    @Override
    public Uni<Void> renameTable(String prefix, IcebergRenameTableRequest icebergRenameTableRequest) {
        return null;
    }

    @Override
    public Uni<Void> renameView(String prefix, IcebergRenameTableRequest icebergRenameTableRequest) {
        return null;
    }

    @Override
    public Uni<IcebergLoadViewResult> replaceView(String prefix, String namespace, String view, IcebergCommitViewRequest icebergCommitViewRequest) {
        return null;
    }

    @Override
    public Uni<Void> reportMetrics(String prefix, String namespace, String table, IcebergReportMetricsRequest icebergReportMetricsRequest) {
        return null;
    }

    @Override
    public Uni<Void> tableExists(String prefix, String namespace, String table) {
        return null;
    }

    @Override
    public Uni<IcebergUpdateNamespacePropertiesResponse> updateProperties(String prefix, String namespace, IcebergUpdateNamespacePropertiesRequest icebergUpdateNamespacePropertiesRequest) {
        return null;
    }

    @Override
    public Uni<IcebergCommitTableResponse> updateTable(String prefix, String namespace, String table, IcebergCommitTableRequest icebergCommitTableRequest) {
        return null;
    }

    @Override
    public Uni<Void> viewExists(String prefix, String namespace, String view) {
        return null;
    }
}
