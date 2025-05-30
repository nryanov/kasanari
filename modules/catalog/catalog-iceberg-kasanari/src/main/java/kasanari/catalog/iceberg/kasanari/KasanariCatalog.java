package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.kasanari.repository.CatalogRepository;
import kasanari.catalog.iceberg.kasanari.repository.NamespaceRepository;
import kasanari.catalog.iceberg.kasanari.repository.TableRepository;
import kasanari.catalog.iceberg.kasanari.repository.ViewRepository;
import kasanari.catalog.iceberg.kasanari.repository.jdbc.JdbcCatalogRepository;
import kasanari.catalog.iceberg.kasanari.repository.jdbc.JdbcNamespaceRepository;
import kasanari.catalog.iceberg.kasanari.repository.jdbc.JdbcTableInitializer;
import kasanari.catalog.iceberg.kasanari.repository.jdbc.JdbcTableRepository;
import kasanari.catalog.iceberg.kasanari.repository.jdbc.JdbcViewRepository;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KasanariCatalog extends BaseMetastoreViewCatalog implements SupportsNamespaces, Configurable<Object> {
    private String catalogName;
    private String warehouse;
    private KasanariDataSource dataSource;

    private CatalogRepository catalogRepository;
    private NamespaceRepository namespaceRepository;
    private TableRepository tableRepository;
    private ViewRepository viewRepository;

    @Override
    public void initialize(String catalogName, Map<String, String> properties) {
        this.catalogName = catalogName;
        this.dataSource = new KasanariDataSource(properties);

        this.warehouse = properties.get(KasanariCatalogProperties.WAREHOUSE);
        if (this.warehouse == null) {
            throw new IllegalArgumentException("Warehouse location is not set");
        }

        this.catalogRepository = new JdbcCatalogRepository(this.dataSource, this.catalogName);
        this.namespaceRepository = new JdbcNamespaceRepository(this.dataSource, this.catalogName);
        this.tableRepository = new JdbcTableRepository(this.dataSource, this.catalogName);
        this.viewRepository = new JdbcViewRepository(this.dataSource, this.catalogName);

        initialize();
    }

    private void initialize() {
        var initializer = new JdbcTableInitializer(dataSource);
        initializer.initialize();
        catalogRepository.registerCurrentCatalog();
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
        namespaceRepository.createNamespace(namespace, metadata);
    }

    @Override
    public List<Namespace> listNamespaces(Namespace namespace) throws NoSuchNamespaceException {
        return namespaceRepository.listNamespaces(namespace);
    }

    @Override
    public Map<String, String> loadNamespaceMetadata(Namespace namespace) throws NoSuchNamespaceException {
        return namespaceRepository.loadNamespaceMetadata(namespace);
    }

    @Override
    public boolean dropNamespace(Namespace namespace) throws NamespaceNotEmptyException {
        return namespaceRepository.dropNamespace(namespace);
    }

    @Override
    public boolean setProperties(Namespace namespace, Map<String, String> properties) throws NoSuchNamespaceException {
        return namespaceRepository.setProperties(namespace, properties);
    }

    @Override
    public boolean removeProperties(Namespace namespace, Set<String> properties) throws NoSuchNamespaceException {
        return namespaceRepository.removeProperties(namespace, properties);
    }

    @Override
    public void close() throws IOException {
        super.close();
        dataSource.close();
    }

    // for testing only
    KasanariDataSource getDataSource() {
        return dataSource;
    }
}
