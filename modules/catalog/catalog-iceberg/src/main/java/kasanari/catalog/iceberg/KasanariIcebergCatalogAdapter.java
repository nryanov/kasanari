package kasanari.catalog.iceberg;


import kasanari.catalog.iceberg.operations.KasanariTableOperations;
import org.apache.iceberg.KasanariMultiTableTransaction;
import org.apache.iceberg.KasanariTransactions;
import org.apache.iceberg.rest.requests.UpdateTableRequest;

import java.util.ArrayList;
import java.util.List;

public class KasanariIcebergCatalogAdapter extends DefaultIcebergCatalogAdapter {
    private final KasanariIcebergCatalog catalog;
    private final boolean enableMultiTableTransaction;

    public KasanariIcebergCatalogAdapter(KasanariIcebergCatalog catalog) {
        this(catalog, true);
    }

    public KasanariIcebergCatalogAdapter(
            KasanariIcebergCatalog catalog,
            boolean enableMultiTAbleTransaction
    ) {
        super(catalog);
        this.catalog = catalog;
        this.enableMultiTableTransaction = enableMultiTAbleTransaction;
    }

    @Override
    public void commitTransaction(List<UpdateTableRequest> transactions) {
        if (enableMultiTableTransaction) {
            commitMultiTableTransaction(transactions);
        } else {
            super.commitTransaction(transactions);
        }
    }

    private void commitMultiTableTransaction(List<UpdateTableRequest> transactions) {
        var awaitingTransactions = new ArrayList<KasanariMultiTableTransaction>();

        transactions.forEach(tx -> {
            var tableIdentifier = tx.identifier();
            var loadedTable = asBaseTable(catalog.loadTable(tableIdentifier));
            var loadedTableOps = (KasanariTableOperations) loadedTable.operations();

            var openedTx = KasanariTransactions.newTransaction(tableIdentifier.toString(), loadedTableOps);
            awaitingTransactions.add(openedTx);

            var ops = (KasanariMultiTableTransaction.TransactionTable) openedTx.table();

            commitTableUpdates(tx, ops.operations());
        });

        catalog.getDataSource().getJdbi().useTransaction(tx -> {
            // atomically commit all changes
            awaitingTransactions.forEach(it -> it.commitSimpleTransaction(tx));
            // cleanup after succeed
            awaitingTransactions.forEach(KasanariMultiTableTransaction::cleanupAfterSimpleTransaction);
        });
    }

}
