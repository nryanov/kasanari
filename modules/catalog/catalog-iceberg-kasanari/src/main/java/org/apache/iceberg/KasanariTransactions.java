package org.apache.iceberg;

public class KasanariTransactions {
    public static KasanariMultiTableTransaction newTransaction(String tableName, TableOperations ops) {
        return new KasanariMultiTableTransaction(tableName, ops, KasanariMultiTableTransaction.TransactionType.SIMPLE, ops.refresh());
    }
}
