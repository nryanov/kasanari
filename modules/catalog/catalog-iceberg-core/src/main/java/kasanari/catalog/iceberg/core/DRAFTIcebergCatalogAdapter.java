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

    public void getToken(String grantType, String scope, String clientId, String clientSecret, Object requestedTokenType, String subjectToken, Object subjectTokenType, String actorToken, Object actorTokenType) {
        // ???
    }

    public void loadCredentials(String prefix, String namespace, String table) {
        // ???
    }

    public void reportMetrics(String prefix, String namespace, String table, Object icebergReportMetricsRequest) {
        // ???
    }

    public void getConfig(String warehouse) {
        // ???
    }

    public void commitTransaction(String prefix, Object icebergCommitTransactionRequest) {
        /*
                List<Transaction> transactions = Lists.newArrayList();
        Iterator var3 = request.tableChanges().iterator();

        while(var3.hasNext()) {
            UpdateTableRequest tableChange = (UpdateTableRequest)var3.next();
            Table table = catalog.loadTable(tableChange.identifier());
            if (!(table instanceof BaseTable)) {
                throw new IllegalStateException("Cannot wrap catalog that does not produce BaseTable");
            }

            Transaction transaction = Transactions.newTransaction(tableChange.identifier().toString(), ((BaseTable)table).operations());
            transactions.add(transaction);
            BaseTransaction.TransactionTable txTable = (BaseTransaction.TransactionTable)transaction.table();
            CatalogHandlers.commit(txTable.operations(), tableChange);
        }

        transactions.forEach(Transaction::commitTransaction);
         */
    }

    public void updateProperties(String prefix, String namespace, Object icebergUpdateNamespacePropertiesRequest) {
        /*
            Set<String> removals = Sets.newHashSet(request.removals());
    Map<String, String> updates = request.updates();

    Map<String, String> startProperties = catalog.loadNamespaceMetadata(namespace);
    Set<String> missing = Sets.difference(removals, startProperties.keySet());

    if (!updates.isEmpty()) {
      catalog.setProperties(namespace, updates);
    }

    if (!removals.isEmpty()) {
      // remove the original set just in case there was an update just after loading properties
      catalog.removeProperties(namespace, removals);
    }

    return UpdateNamespacePropertiesResponse.builder()
        .addMissing(missing)
        .addUpdated(updates.keySet())
        .addRemoved(Sets.difference(removals, missing))
        .build();
         */
    }
}
