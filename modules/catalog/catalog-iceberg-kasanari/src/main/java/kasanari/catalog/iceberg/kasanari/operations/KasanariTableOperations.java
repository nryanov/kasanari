package kasanari.catalog.iceberg.kasanari.operations;

import org.apache.iceberg.BaseMetastoreTableOperations;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.io.FileIO;

public class KasanariTableOperations extends BaseMetastoreTableOperations {
    @Override
    protected String tableName() {
        return "";
    }

    @Override
    public FileIO io() {
        return null;
    }

    @Override
    protected void doRefresh() {
        super.doRefresh();
    }

    @Override
    protected void doCommit(TableMetadata base, TableMetadata metadata) {
        super.doCommit(base, metadata);
    }
}
