package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.annotations.VisibleForTesting;
import kasanari.catalog.iceberg.kasanari.operations.KasanariTableOperations;
import kasanari.catalog.iceberg.kasanari.operations.KasanariViewOperations;
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
import kasanari.catalog.iceberg.kasanari.utils.IcebergUtils;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.NamespaceNotEmptyException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.exceptions.NotFoundException;
import org.apache.iceberg.hadoop.Configurable;
import org.apache.iceberg.io.FileIO;
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

    private FileIO io;
    private Object hadoopConfig;

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

        initializeCatalog();
        initializeFileIO(properties);
    }

    private void initializeCatalog() {
        var initializer = new JdbcTableInitializer(dataSource);
        initializer.initialize();

        // todo: change to upsert ?
        if (catalogRepository.notExists()) {
            catalogRepository.register();
        }
    }

    private void initializeFileIO(Map<String, String> properties) {
        var ioImpl = properties.getOrDefault(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.hadoop.HadoopFileIO");
        this.io = CatalogUtil.loadFileIO(ioImpl, properties, hadoopConfig);
    }

    @Override
    public void setConf(Object conf) {
        this.hadoopConfig = conf;
    }

    @Override
    protected String defaultWarehouseLocation(TableIdentifier tableIdentifier) {
        var namespace = tableIdentifier.namespace();

        if (namespace.isEmpty()) {
            return String.join("/", warehouse, tableIdentifier.name());
        } else {
            return String.join("/", warehouse, String.join("/", namespace.levels()), tableIdentifier.name());
        }
    }

    @Override
    protected ViewOperations newViewOps(TableIdentifier viewIdentifier) {
        return new KasanariViewOperations(namespaceRepository, tableRepository, viewRepository, io, viewIdentifier, catalogName);
    }

    @Override
    public List<TableIdentifier> listViews(Namespace namespace) {
        if (namespaceRepository.notExists(namespace)) {
            throw new NoSuchNamespaceException(
                    "Namespace `%s` does not exist in catalog `%s`",
                    IcebergUtils.namespaceName(namespace),
                    catalogName
            );
        }

        return viewRepository.findByNamespace(namespace);
    }

    @Override
    public boolean dropView(TableIdentifier identifier) {
        return viewRepository.delete(identifier);
    }

    @Override
    public void renameView(TableIdentifier from, TableIdentifier to) {
        if (from.equals(to)) {
            return;
        }

        if (namespaceRepository.notExists(to.namespace())) {
            throw new NoSuchNamespaceException("Cannot rename table because namespace `%s` does not exist", to.namespace());
        }

        if (viewRepository.notExists(from)) {
            throw new NoSuchTableException("View `%s` does not exist", from);
        }

        if (tableRepository.exists(to)) {
            throw new NoSuchTableException("Cannot rename table, because table `%s` is already exists", to);
        }

        if (viewRepository.exists(to)) {
            throw new NoSuchTableException("Cannot rename table, because view `%s` is already exists", to);
        }

        var result = viewRepository.rename(from, to);
        if (!result) {
            throw new NoSuchTableException("View `%s` wasn't renamed because it does not exist", from);
        }
    }

    @Override
    protected TableOperations newTableOps(TableIdentifier tableIdentifier) {
        return new KasanariTableOperations(namespaceRepository, tableRepository, viewRepository, io, tableIdentifier, catalogName);
    }

    @Override
    public List<TableIdentifier> listTables(Namespace namespace) {
        if (!namespace.isEmpty() && namespaceRepository.notExists(namespace)) {
            throw new NoSuchNamespaceException(
                    "Namespace `%s` does not exist in catalog `%s`",
                    IcebergUtils.namespaceName(namespace),
                    catalogName
            );
        }

        return tableRepository.findByNamespace(namespace);
    }

    @Override
    public boolean dropTable(TableIdentifier identifier, boolean purge) {
        var tableOperations = newTableOps(identifier);
        var maybeCurrentTableMetadata = (TableMetadata) null;

        if (purge) {
            try {
                maybeCurrentTableMetadata = tableOperations.current();
            } catch (NotFoundException e) {
                // todo: log warning
            }
        }

        var deleted = tableRepository.delete(identifier);

        if (deleted) {
            if (purge && maybeCurrentTableMetadata != null) {
                CatalogUtil.dropTableData(io, maybeCurrentTableMetadata);
            }

            return true;
        }

        return false;
    }

    @Override
    public void renameTable(TableIdentifier from, TableIdentifier to) {
        if (from.equals(to)) {
            return;
        }

        if (tableRepository.notExists(from)) {
            throw new NoSuchTableException("Table `%s` does not exist", from);
        }

        if (namespaceRepository.notExists(to.namespace())) {
            throw new NoSuchNamespaceException("Cannot rename table because namespace `%s` does not exist", to.namespace());
        }

        if (tableRepository.exists(to)) {
            throw new NoSuchTableException("Cannot rename table, because table `%s` is already exists", to);
        }

        if (viewRepository.exists(to)) {
            throw new NoSuchTableException("Cannot rename table, because view `%s` is already exists", to);
        }

        var result = tableRepository.rename(from, to);
        if (!result) {
            throw new NoSuchTableException("Table `%s` wasn't renamed because it does not exist", from);
        }
    }

    @Override
    public void createNamespace(Namespace namespace, Map<String, String> metadata) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        if (namespaceRepository.exists(namespace)) {
            throw new AlreadyExistsException(String.format("Namespace `%s` is already exists in catalog `%s`", namespaceName, catalogName));
        }

        namespaceRepository.create(namespace, metadata);
    }

    @Override
    public List<Namespace> listNamespaces(Namespace namespace) throws NoSuchNamespaceException {
        return namespaceRepository.list(namespace);
    }

    @Override
    public Map<String, String> loadNamespaceMetadata(Namespace namespace) throws NoSuchNamespaceException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        if (namespaceRepository.notExists(namespace)) {
            throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
        }

        return namespaceRepository.load(namespace);
    }

    @Override
    public boolean dropNamespace(Namespace namespace) throws NamespaceNotEmptyException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        if (namespaceRepository.notExists(namespace)) {
            return false;
        }

        if (namespaceRepository.linkedTablesExist(namespace) || namespaceRepository.linkedViewsExist(namespace)) {
            throw new NamespaceNotEmptyException(String.format("Namespace `%s` in catalog `%s` cannot be dropped because it has linked entities", namespaceName, catalogName));
        }

        return namespaceRepository.delete(namespace);
    }

    @Override
    public boolean setProperties(Namespace namespace, Map<String, String> properties) throws NoSuchNamespaceException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        if (namespaceRepository.notExists(namespace)) {
            throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
        }

        return namespaceRepository.setProperties(namespace, properties);
    }

    @Override
    public boolean removeProperties(Namespace namespace, Set<String> properties) throws NoSuchNamespaceException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        if (namespaceRepository.notExists(namespace)) {
            throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
        }

        return namespaceRepository.removeProperties(namespace, properties);
    }

    @Override
    public void close() throws IOException {
        super.close();
        dataSource.close();
    }

    @Override
    public String toString() {
        return catalogName;
    }

    KasanariDataSource getDataSource() {
        return dataSource;
    }

    @VisibleForTesting
    TableRepository getTableRepository() {
        return tableRepository;
    }

    @VisibleForTesting
    void setTableRepository(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
}
