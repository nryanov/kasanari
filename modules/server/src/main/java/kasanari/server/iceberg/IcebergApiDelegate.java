package kasanari.server.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.iceberg.IcebergApi;
import kasanari.api.iceberg.dto.*;

@ApplicationScoped
public class IcebergApiDelegate implements IcebergApi {
    @Override
    public void cancelPlanning(String prefix, String namespace, String table, String planId) {

    }

    @Override
    public void commitTransaction(String prefix, IcebergCommitTransactionRequestDto icebergCommitTransactionRequestDto) {

    }

    @Override
    public IcebergCreateNamespaceResponseDto createNamespace(String prefix, IcebergCreateNamespaceRequestDto icebergCreateNamespaceRequestDto) {
        return null;
    }

    @Override
    public IcebergLoadTableResultDto createTable(String prefix, String namespace, IcebergCreateTableRequestDto icebergCreateTableRequestDto, String xIcebergAccessDelegation) {
        return null;
    }

    @Override
    public IcebergLoadViewResultDto createView(String prefix, String namespace, IcebergCreateViewRequestDto icebergCreateViewRequestDto) {
        return null;
    }

    @Override
    public void dropNamespace(String prefix, String namespace) {

    }

    @Override
    public void dropTable(String prefix, String namespace, String table, Boolean purgeRequested) {

    }

    @Override
    public void dropView(String prefix, String namespace, String view) {

    }

    @Override
    public IcebergFetchPlanningResultDto fetchPlanningResult(String prefix, String namespace, String table, String planId) {
        return null;
    }

    @Override
    public IcebergFetchScanTasksResultDto fetchScanTasks(String prefix, String namespace, String table, IcebergFetchScanTasksRequestDto icebergFetchScanTasksRequestDto) {
        return null;
    }

    @Override
    public IcebergCatalogConfigDto getConfig(String warehouse) {
        return null;
    }

    @Override
    public IcebergOAuthTokenResponseDto getToken(String grantType, String scope, String clientId, String clientSecret, IcebergTokenTypeDto requestedTokenType, String subjectToken, IcebergTokenTypeDto subjectTokenType, String actorToken, IcebergTokenTypeDto actorTokenType) {
        return null;
    }

    @Override
    public IcebergListNamespacesResponseDto listNamespaces(String prefix, String pageToken, Integer pageSize, String parent) {
        return null;
    }

    @Override
    public IcebergListTablesResponseDto listTables(String prefix, String namespace, String pageToken, Integer pageSize) {
        return null;
    }

    @Override
    public IcebergListTablesResponseDto listViews(String prefix, String namespace, String pageToken, Integer pageSize) {
        return null;
    }

    @Override
    public IcebergLoadCredentialsResponseDto loadCredentials(String prefix, String namespace, String table) {
        return null;
    }

    @Override
    public IcebergGetNamespaceResponseDto loadNamespaceMetadata(String prefix, String namespace) {
        return null;
    }

    @Override
    public IcebergLoadTableResultDto loadTable(String prefix, String namespace, String table, String xIcebergAccessDelegation, String ifNoneMatch, String snapshots) {
        return null;
    }

    @Override
    public IcebergLoadViewResultDto loadView(String prefix, String namespace, String view) {
        return null;
    }

    @Override
    public void namespaceExists(String prefix, String namespace) {

    }

    @Override
    public IcebergPlanTableScanResultDto planTableScan(String prefix, String namespace, String table, IcebergPlanTableScanRequestDto icebergPlanTableScanRequestDto) {
        return null;
    }

    @Override
    public IcebergLoadTableResultDto registerTable(String prefix, String namespace, IcebergRegisterTableRequestDto icebergRegisterTableRequestDto) {
        return null;
    }

    @Override
    public void renameTable(String prefix, IcebergRenameTableRequestDto icebergRenameTableRequestDto) {

    }

    @Override
    public void renameView(String prefix, IcebergRenameTableRequestDto icebergRenameTableRequestDto) {

    }

    @Override
    public IcebergLoadViewResultDto replaceView(String prefix, String namespace, String view, IcebergCommitViewRequestDto icebergCommitViewRequestDto) {
        return null;
    }

    @Override
    public void reportMetrics(String prefix, String namespace, String table, IcebergReportMetricsRequestDto icebergReportMetricsRequestDto) {

    }

    @Override
    public void tableExists(String prefix, String namespace, String table) {

    }

    @Override
    public IcebergUpdateNamespacePropertiesResponseDto updateProperties(String prefix, String namespace, IcebergUpdateNamespacePropertiesRequestDto icebergUpdateNamespacePropertiesRequestDto) {
        return null;
    }

    @Override
    public IcebergCommitTableResponseDto updateTable(String prefix, String namespace, String table, IcebergCommitTableRequestDto icebergCommitTableRequestDto) {
        return null;
    }

    @Override
    public void viewExists(String prefix, String namespace, String view) {

    }
}
