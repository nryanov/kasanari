package kasanari.catalog.iceberg.operations;

import kasanari.repository.iceberg.NamespaceRepository;
import kasanari.repository.iceberg.TableRepository;
import kasanari.repository.iceberg.ViewRepository;
import kasanari.repository.iceberg.model.IcebergTableRecord;
import kasanari.repository.iceberg.IcebergUtils;
import kasanari.repository.core.TransactionManager;
import org.apache.iceberg.BaseMetastoreTableOperations;
import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.io.FileIO;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class KasanariTableOperations extends BaseMetastoreTableOperations {
    private final static Logger logger = LoggerFactory.getLogger(KasanariTableOperations.class);

    private final TransactionManager<Handle> transactionManager;
    private final NamespaceRepository<Handle> namespaceRepository;
    private final TableRepository<Handle> tableRepository;
    private final ViewRepository<Handle> viewRepository;
    private final FileIO fileIO;
    private final TableIdentifier tableIdentifier;
    private final String catalogName;

    public KasanariTableOperations(
            TransactionManager<Handle> transactionManager,
            NamespaceRepository<Handle> namespaceRepository,
            TableRepository<Handle> tableRepository,
            ViewRepository<Handle> viewRepository,
            FileIO fileIO,
            TableIdentifier tableIdentifier,
            String catalogName
    ) {
        this.transactionManager = transactionManager;
        this.namespaceRepository = namespaceRepository;
        this.tableRepository = tableRepository;
        this.viewRepository = viewRepository;
        this.fileIO = fileIO;
        this.tableIdentifier = tableIdentifier;
        this.catalogName = catalogName;
    }

    @Override
    protected String tableName() {
        return tableIdentifier.toString();
    }

    @Override
    public FileIO io() {
        return fileIO;
    }

    @Override
    protected void doRefresh() {
        // todo: optimize
        transactionManager.inTransaction(tx -> {
            if (tableRepository.exists(tx, tableIdentifier)) {
                var table = tableRepository.load(tx, tableIdentifier);

                if (table.metadataLocation() == null) {
                    throw new ValidationException("State of table `%s` is incorrect: metadata location is null", tableIdentifier);
                }

                refreshFromMetadataLocation(table.metadataLocation());
            } else {
                // if table does not exist but there is a metadata info
                if (currentMetadataLocation() != null) {
                    throw new NoSuchTableException(
                            "Table `%s` couldn't be loaded from catalog `%s` because it was dropped",
                            tableIdentifier.toString(), catalogName
                    );
                } else {
                    // table does not exist and there is no existing metadata
                    disableRefresh();
                }
            }
        });
    }


    // multi table commit tx handler
    public void commit(Handle tx, TableMetadata base, TableMetadata metadata) {
        // if the metadata is already out of date, reject it
        if (base != current()) {
            if (base != null) {
                throw new CommitFailedException("Cannot commit: stale table metadata");
            } else {
                // when current is non-null, the table exists. but when base is null, the commit is trying
                // to create the table
                throw new AlreadyExistsException("Table already exists: %s", tableName());
            }
        }
        // if the metadata is not changed, return early
        if (base == metadata) {
            logger.info("Nothing to commit.");
            return;
        }

        long start = System.currentTimeMillis();
        doCommit(tx, base, metadata);
        CatalogUtil.deleteRemovedMetadataFiles(io(), base, metadata);
        requestRefresh();

        logger.info("Successfully committed to table {} in {} ms", tableName(), System.currentTimeMillis() - start);
    }

    protected void doCommit(Handle tx, TableMetadata base, TableMetadata metadata) {
        var isNewTable = base == null;
        var newMetadataLocation = writeNewMetadataIfRequired(isNewTable, metadata);

        var failure = false;

        try {
            if (isNewTable) {
                throw new IllegalStateException("Only DMK operations supported for multi-table transactions");
            } else {
                logger.debug("Updating table: {}", tableIdentifier);
                var existingTable = tableRepository.load(tx, tableIdentifier);
                // check that current location didn't change yet
                validateMetadataLocation(existingTable, base);
                updateTable(tx, existingTable.metadataLocation(), newMetadataLocation);
            }
        } catch (Exception e) {
            logger.warn("Error happened while commiting to table `{}`: {}", tableIdentifier, e.getMessage());
            failure = true;
            throw e;
        } finally {
            if (failure) {
                logger.warn("Deleting metadata file `{}` due to error for table `{}`", newMetadataLocation, tableIdentifier);
                fileIO.deleteFile(newMetadataLocation);
            }
        }
    }

    @Override
    protected void doCommit(TableMetadata base, TableMetadata metadata) {
        var isNewTable = base == null;
        var newMetadataLocation = writeNewMetadataIfRequired(isNewTable, metadata);

        var failure = false;

        try {
            if (isNewTable) {
                logger.debug("Creating new table: {}", tableIdentifier);
                createTable(newMetadataLocation);
            } else {
                logger.debug("Updating table: {}", tableIdentifier);
                transactionManager.inTransaction(tx -> {
                    var existingTable = tableRepository.load(tx, tableIdentifier);
                    // check that current location didn't change yet
                    validateMetadataLocation(existingTable, base);
                    updateTable(tx, existingTable.metadataLocation(), newMetadataLocation);
                });
            }
        } catch (Exception e) {
            logger.warn("Error happened while commiting to table `{}`: {}", tableIdentifier, e.getMessage());
            failure = true;
            throw e;
        } finally {
            if (failure) {
                logger.warn("Deleting metadata file `{}` due to error for table `{}`", newMetadataLocation, tableIdentifier);
                fileIO.deleteFile(newMetadataLocation);
            }
        }
    }

    private void validateMetadataLocation(IcebergTableRecord table, TableMetadata base) {
        String catalogMetadataLocation = table.metadataLocation();
        String baseMetadataLocation = base != null ? base.metadataFileLocation() : null;

        if (!Objects.equals(baseMetadataLocation, catalogMetadataLocation)) {
            throw new CommitFailedException(
                    "Cannot commit %s: metadata location %s has changed from %s",
                    tableIdentifier, baseMetadataLocation, catalogMetadataLocation);
        }
    }

    private void createTable(String newMetadataLocation) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        var result = transactionManager.inTransactionR(tx -> {
            if (!namespaceRepository.exists(tx, tableIdentifier.namespace())) {
                throw new NoSuchNamespaceException(
                        "Table couldn't be created because namespace `%s` does not exist in catalog `%s`",
                        namespaceName,
                        catalogName
                );
            }

            if (viewRepository.exists(tx, tableIdentifier)) {
                throw new AlreadyExistsException(
                        "Table couldn't be created because view with the same name `%s` is already exist in catalog `%s`",
                        tableIdentifier.toString(),
                        catalogName
                );
            }

            if (tableRepository.exists(tx, tableIdentifier)) {
                throw new AlreadyExistsException(
                        "Table couldn't be created because table with the same name `%s` is already exist in catalog `%s`",
                        tableIdentifier.toString(),
                        catalogName
                );
            }

            return tableRepository.create(tx, tableIdentifier, newMetadataLocation);
        });

        if (!result) {
            throw new CommitFailedException(
                    "Table `%s` wasn't created in catalog `%s`",
                    tableIdentifier,
                    catalogName
            );
        }
    }

    private void updateTable(Handle tx, String previousMetadataLocation, String newMetadataLocation) {
        var result = tableRepository.update(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation);

        if (!result) {
            throw new CommitFailedException(
                    "Table `%s` wasn't updated in catalog `%s`",
                    tableIdentifier.toString(),
                    catalogName
            );
        }
    }
}
