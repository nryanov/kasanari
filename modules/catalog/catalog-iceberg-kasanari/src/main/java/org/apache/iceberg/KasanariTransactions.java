package org.apache.iceberg;

import kasanari.catalog.iceberg.kasanari.operations.KasanariTableOperations;

public class KasanariTransactions {
    public static KasanariMultiTableTransaction newTransaction(String tableName, KasanariTableOperations ops) {
        return new KasanariMultiTableTransaction(tableName, ops, KasanariMultiTableTransaction.TransactionType.SIMPLE, ops.refresh());
    }
}
