package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.DefaultIcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.model.IcebergTable;
import kasanari.catalog.iceberg.kasanari.operations.KasanariTableOperations;
import org.apache.iceberg.KasanariMultiTableTransaction;
import org.apache.iceberg.KasanariTransactions;

import java.util.ArrayList;
import java.util.List;

public class KasanariIcebergCatalogAdapter extends DefaultIcebergCatalogAdapter {
    private final KasanariCatalog catalog;

    public KasanariIcebergCatalogAdapter(KasanariCatalog catalog) {
        super(catalog);
        this.catalog = catalog;
    }

    @Override
    public void commitTransaction(List<IcebergTable.Transaction> transactions) {
        var awaitingTransactions = new ArrayList<KasanariMultiTableTransaction>();

        transactions.forEach(tx -> {
            var tableIdentifier = tx.table().toIceberg();
            var loadedTable = asBaseTable(catalog.loadTable(tableIdentifier));
            var loadedTableOps = (KasanariTableOperations) loadedTable.operations();

            var openedTx = KasanariTransactions.newTransaction(tableIdentifier.toString(), loadedTableOps);
            awaitingTransactions.add(openedTx);

            var updates = tx.changes().toIceberg(tableIdentifier);
            var ops = (KasanariMultiTableTransaction.TransactionTable) openedTx.table();

            commitTableUpdates(updates, ops.operations());
        });

        catalog.getDataSource().getJdbi().useTransaction(tx -> {
            // atomically commit all changes
            awaitingTransactions.forEach(it -> {
                it.commitSimpleTransaction(tx);
            });

            // cleanup after succeed
            awaitingTransactions.forEach(KasanariMultiTableTransaction::cleanupAfterSimpleTransaction);
        });
    }

}
