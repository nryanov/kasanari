package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.kasanari.repository.jdbc.KasanariDataSource;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.NamespaceNotEmptyException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.apache.iceberg.hadoop.Configurable;
import org.apache.iceberg.view.BaseMetastoreViewCatalog;
import org.apache.iceberg.view.ViewOperations;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class KasanariCatalog extends BaseMetastoreViewCatalog implements SupportsNamespaces, Configurable<Object> {
    private String name;
    private String warehouse;
    private KasanariDataSource dataSource;

    @Override
    public void initialize(String name, Map<String, String> properties) {
        this.name = name;
        this.dataSource = new KasanariDataSource(properties);

        this.warehouse = properties.get(KasanariCatalogProperties.WAREHOUSE);
        if (this.warehouse == null) {
            throw new IllegalArgumentException("Warehouse location is not set");
        }
    }

    private void initializeTables() {

    }

    @Override
    public void setConf(Object conf) {

    }

    @Override
    protected String defaultWarehouseLocation(TableIdentifier tableIdentifier) {
        return "";
    }

    @Override
    protected ViewOperations newViewOps(TableIdentifier viewIdentifier) {
        return null;
    }

    @Override
    public List<TableIdentifier> listViews(Namespace namespace) {
        return List.of();
    }

    @Override
    public boolean dropView(TableIdentifier identifier) {
        return false;
    }

    @Override
    public void renameView(TableIdentifier from, TableIdentifier to) {

    }

    @Override
    protected TableOperations newTableOps(TableIdentifier tableIdentifier) {
        return null;
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
    public void createNamespace(Namespace namespace, Map<String, String> metadata) {
    }

    @Override
    public List<Namespace> listNamespaces(Namespace namespace) throws NoSuchNamespaceException {
        return List.of();
    }

    @Override
    public Map<String, String> loadNamespaceMetadata(Namespace namespace) throws NoSuchNamespaceException {
        return Map.of();
    }

    @Override
    public boolean dropNamespace(Namespace namespace) throws NamespaceNotEmptyException {
        return false;
    }

    @Override
    public boolean setProperties(Namespace namespace, Map<String, String> properties) throws NoSuchNamespaceException {
        return false;
    }

    @Override
    public boolean removeProperties(Namespace namespace, Set<String> properties) throws NoSuchNamespaceException {
        return false;
    }
}
