package kasanari.catalog.iceberg.core;

import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.ViewCatalog;

public class DRAFTIcebergCatalogAdapter {
    private Catalog catalog;
    private SupportsNamespaces asNamespaceCatalog;
    private ViewCatalog asViewCatalog;

    public void cancelPlanning(String prefix, String namespace, String table, String planId) {
        // ???
    }

    public void fetchPlanningResult(String prefix, String namespace, String table, String planId) {
        // ???
    }

    public void fetchScanTasks(String prefix, String namespace, String table, Object icebergFetchScanTasksRequest) {
        // ???
    }

    public void planTableScan(String prefix, String namespace, String table, Object icebergPlanTableScanRequest) {
        // ???
    }

    public void reportMetrics(String prefix, String namespace, String table, Object icebergReportMetricsRequest) {
        // ???
    }

    public void getToken(String grantType, String scope, String clientId, String clientSecret, Object requestedTokenType, String subjectToken, Object subjectTokenType, String actorToken, Object actorTokenType) {
        // ???
    }

    public void getConfig(String warehouse) {
        // ???
    }

    public void loadCredentials(String prefix, String namespace, String table) {
        // ???
    }
}
