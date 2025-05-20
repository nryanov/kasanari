package kasanari.catalog.iceberg.kasanari;

import org.apache.iceberg.BaseMetastoreCatalog;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;

import java.util.List;
import java.util.Map;

public class KasanariCatalog extends BaseMetastoreCatalog {
    @Override
    protected TableOperations newTableOps(TableIdentifier tableIdentifier) {
        return null;
    }

    @Override
    protected String defaultWarehouseLocation(TableIdentifier tableIdentifier) {
        return "";
    }

    @Override
    public List<TableIdentifier> listTables(Namespace namespace) {
        return List.of();
    }

    @Override
    public boolean dropTable(TableIdentifier identifier, boolean purge) {
        return false;
    }

    @Override
    public void renameTable(TableIdentifier from, TableIdentifier to) {

    }

    @Override
    public void initialize(String name, Map<String, String> properties) {
        super.initialize(name, properties);
    }
}
