package kasanari.catalog.iceberg;


import kasanari.catalog.iceberg.annotations.VisibleForTesting;
import kasanari.catalog.iceberg.operations.KasanariTableOperations;
import kasanari.catalog.iceberg.operations.KasanariViewOperations;
import kasanari.repository.iceberg.CatalogRepository;
import kasanari.repository.iceberg.IcebergRepositoryBundleFactory;
import kasanari.repository.iceberg.IcebergUtils;
import kasanari.repository.iceberg.NamespaceRepository;
import kasanari.repository.iceberg.TableRepository;
import kasanari.repository.iceberg.ViewRepository;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.BackendFactoryLoader;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
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
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KasanariIcebergCatalog extends BaseMetastoreViewCatalog implements SupportsNamespaces, Configurable<Object> {
    private final static Logger logger = LoggerFactory.getLogger(KasanariIcebergCatalog.class);

    private String catalogName;
    private String warehouse;
    private KasanariDataSource dataSource;

    private TransactionManager<Handle> transactionManager;
    private CatalogRepository<Handle> catalogRepository;
    private NamespaceRepository<Handle> namespaceRepository;
    private TableRepository<Handle> tableRepository;
    private ViewRepository<Handle> viewRepository;

    private FileIO io;
    private Object hadoopConfig;

    @Override
    public void initialize(String catalogName, Map<String, String> properties) {
        this.catalogName = catalogName;
        this.dataSource = new KasanariDataSource(properties);

        this.warehouse = properties.get("warehouse");
        if (this.warehouse == null) {
            throw new IllegalArgumentException("Warehouse location is not set");
        }

        this.transactionManager = new JdbcTransactionManager(dataSource);
        var bundle = BackendFactoryLoader.load(
                IcebergRepositoryBundleFactory.class,
                dataSource.repositoryBackend()
        ).create(this.catalogName, dataSource);
        this.catalogRepository = bundle.catalogRepository();
        this.namespaceRepository = bundle.namespaceRepository();
        this.tableRepository = bundle.tableRepository();
        this.viewRepository = bundle.viewRepository();

        bundle.schemaInitializer().run();
        transactionManager.inTransaction(tx -> catalogRepository.register(tx));
        initializeFileIO(properties);
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
    public boolean tableExists(TableIdentifier identifier) {
        return transactionManager.inTransactionR(tx -> tableRepository.exists(tx, identifier));
    }

    @Override
    public boolean viewExists(TableIdentifier identifier) {
        return transactionManager.inTransactionR(tx -> viewRepository.exists(tx, identifier));
    }

    @Override
    protected ViewOperations newViewOps(TableIdentifier viewIdentifier) {
        return new KasanariViewOperations(
                transactionManager,
                namespaceRepository,
                tableRepository,
                viewRepository,
                io,
                viewIdentifier,
                catalogName
        );
    }

    @Override
    public List<TableIdentifier> listViews(Namespace namespace) {
        return transactionManager.inTransactionR(tx -> {
            if (namespaceRepository.notExists(tx, namespace)) {
                throw new NoSuchNamespaceException(
                        "Namespace `%s` does not exist in catalog `%s`",
                        IcebergUtils.namespaceName(namespace),
                        catalogName
                );
            }

            return viewRepository.findByNamespace(tx, namespace);
        });
    }

    @Override
    public boolean dropView(TableIdentifier identifier) {
        return transactionManager.inTransactionR(tx -> viewRepository.delete(tx, identifier));
    }

    @Override
    public void renameView(TableIdentifier from, TableIdentifier to) {
        if (from.equals(to)) {
            return;
        }

        transactionManager.inTransaction(tx -> {
            if (namespaceRepository.notExists(tx, to.namespace())) {
                throw new NoSuchNamespaceException("Cannot rename table because namespace `%s` does not exist", to.namespace());
            }

            if (viewRepository.notExists(tx, from)) {
                throw new NoSuchTableException("View `%s` does not exist", from);
            }

            if (tableRepository.exists(tx, to)) {
                throw new NoSuchTableException("Cannot rename table, because table `%s` is already exists", to);
            }

            if (viewRepository.exists(tx, to)) {
                throw new NoSuchTableException("Cannot rename table, because view `%s` is already exists", to);
            }

            var result = viewRepository.rename(tx, from, to);
            if (!result) {
                throw new NoSuchTableException("View `%s` wasn't renamed because it does not exist", from);
            }
        });
    }

    @Override
    protected TableOperations newTableOps(TableIdentifier tableIdentifier) {
        return new KasanariTableOperations(
                transactionManager,
                namespaceRepository,
                tableRepository,
                viewRepository,
                io,
                tableIdentifier,
                catalogName
        );
    }

    @Override
    public List<TableIdentifier> listTables(Namespace namespace) {
        return transactionManager.inTransactionR(tx -> {
            if (!namespace.isEmpty() && namespaceRepository.notExists(tx, namespace)) {
                throw new NoSuchNamespaceException(
                        "Namespace `%s` does not exist in catalog `%s`",
                        IcebergUtils.namespaceName(namespace),
                        catalogName
                );
            }

            return tableRepository.findByNamespace(tx, namespace);
        });
    }

    @Override
    public boolean dropTable(TableIdentifier identifier, boolean purge) {
        var tableOperations = newTableOps(identifier);
        var maybeCurrentTableMetadata = (TableMetadata) null;

        if (purge) {
            try {
                maybeCurrentTableMetadata = tableOperations.current();
            } catch (NotFoundException e) {
                logger.warn("Attempt to drop not existing table {}", identifier);
            }
        }

        final var lambdaMaybeCurrentTableMetadata = maybeCurrentTableMetadata;
        return transactionManager.inTransactionR(tx -> {
            var deleted = tableRepository.delete(tx, identifier);

            if (deleted) {
                if (purge && lambdaMaybeCurrentTableMetadata != null) {
                    CatalogUtil.dropTableData(io, lambdaMaybeCurrentTableMetadata);
                }

                return true;
            }

            return false;
        });
    }

    @Override
    public void renameTable(TableIdentifier from, TableIdentifier to) {
        if (from.equals(to)) {
            return;
        }

        transactionManager.inTransaction(tx -> {
            if (tableRepository.notExists(tx, from)) {
                throw new NoSuchTableException("Table `%s` does not exist", from);
            }

            if (namespaceRepository.notExists(tx, to.namespace())) {
                throw new NoSuchNamespaceException("Cannot rename table because namespace `%s` does not exist", to.namespace());
            }

            if (tableRepository.exists(tx, to)) {
                throw new NoSuchTableException("Cannot rename table, because table `%s` is already exists", to);
            }

            if (viewRepository.exists(tx, to)) {
                throw new NoSuchTableException("Cannot rename table, because view `%s` is already exists", to);
            }

            var result = tableRepository.rename(tx, from, to);
            if (!result) {
                throw new NoSuchTableException("Table `%s` wasn't renamed because it does not exist", from);
            }
        });
    }

    @Override
    public void createNamespace(Namespace namespace, Map<String, String> metadata) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        transactionManager.inTransaction(tx -> {
            if (namespaceRepository.exists(tx, namespace)) {
                throw new AlreadyExistsException(String.format("Namespace `%s` is already exists in catalog `%s`", namespaceName, catalogName));
            }

            namespaceRepository.create(tx, namespace, metadata);
        });
    }

    @Override
    public List<Namespace> listNamespaces(Namespace namespace) throws NoSuchNamespaceException {
        return transactionManager.inTransactionR(tx -> namespaceRepository.list(tx, namespace));
    }

    @Override
    public Map<String, String> loadNamespaceMetadata(Namespace namespace) throws NoSuchNamespaceException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        return transactionManager.inTransactionR(tx -> {
            if (namespaceRepository.notExists(tx, namespace)) {
                throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
            }

            return namespaceRepository.load(tx, namespace);
        });
    }

    @Override
    public boolean dropNamespace(Namespace namespace) throws NamespaceNotEmptyException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        return transactionManager.inTransactionR(tx -> {
            if (namespaceRepository.notExists(tx, namespace)) {
                return false;
            }

            if (namespaceRepository.linkedTablesExist(tx, namespace) || namespaceRepository.linkedViewsExist(tx, namespace)) {
                throw new NamespaceNotEmptyException(String.format("Namespace `%s` in catalog `%s` cannot be dropped because it has linked entities", namespaceName, catalogName));
            }

            return namespaceRepository.delete(tx, namespace);
        });
    }

    @Override
    public boolean setProperties(Namespace namespace, Map<String, String> properties) throws NoSuchNamespaceException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        return transactionManager.inTransactionR(tx -> {
            if (namespaceRepository.notExists(tx, namespace)) {
                throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
            }

            return namespaceRepository.setProperties(tx, namespace, properties);
        });
    }

    @Override
    public boolean removeProperties(Namespace namespace, Set<String> properties) throws NoSuchNamespaceException {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        return transactionManager.inTransactionR(tx -> {
            if (namespaceRepository.notExists(tx, namespace)) {
                throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
            }

            return namespaceRepository.removeProperties(tx, namespace, properties);
        });
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

    @VisibleForTesting
    KasanariDataSource getDataSource() {
        return dataSource;
    }

    @VisibleForTesting
    TableRepository<Handle> getTableRepository() {
        return tableRepository;
    }

    @VisibleForTesting
    void setTableRepository(TableRepository<Handle> tableRepository) {
        this.tableRepository = tableRepository;
    }
}
