package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.DefaultIcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.model.IcebergTable;
import org.apache.iceberg.BaseTransaction;
import org.apache.iceberg.KasanariTransactions;
import org.apache.iceberg.Transaction;

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
        System.out.println("Commit");
        var awaitingTransactions = new ArrayList<Transaction>();

        transactions.forEach(tx -> {
            var tableIdentifier = tx.table().toIceberg();
            var loadedTable = asBaseTable(catalog.loadTable(tableIdentifier));
            var openedTx = KasanariTransactions.newTransaction(tableIdentifier.toString(), loadedTable.operations());
            awaitingTransactions.add(openedTx);

            var updates = tx.changes().toIceberg(tableIdentifier);
            var ops = (BaseTransaction.TransactionTable) openedTx.table();

            commitTableUpdates(updates, ops.operations());
        });

        catalog.getDataSource().getJdbi().useTransaction(tx -> {

        });
        awaitingTransactions.forEach(Transaction::commitTransaction);
    }

}
