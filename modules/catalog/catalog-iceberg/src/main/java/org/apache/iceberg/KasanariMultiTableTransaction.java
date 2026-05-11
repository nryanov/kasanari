package org.apache.iceberg;

import kasanari.catalog.iceberg.operations.KasanariTableOperations;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.exceptions.CleanableFailure;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.CommitStateUnknownException;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;
import org.apache.iceberg.metrics.LoggingMetricsReporter;
import org.apache.iceberg.metrics.MetricsReporter;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.Tasks;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT;

// copy of BaseTableTransaction but with a little bit of different implementation of commitSimpleTransaction method
public class KasanariMultiTableTransaction implements Transaction {
    private static final Logger logger = LoggerFactory.getLogger(KasanariMultiTableTransaction.class);

    public enum TransactionType {
        CREATE_TABLE,
        REPLACE_TABLE,
        CREATE_OR_REPLACE_TABLE,
        SIMPLE
    }

    private final String tableName;
    private final KasanariTableOperations ops;
    private final TransactionTable transactionTable;
    private final TableOperations transactionOps;
    private final List<PendingUpdate> updates;
    private final Set<String> deletedFiles = new HashSet<>(); // keep track of files deleted in the most recent commit
    private final Consumer<String> enqueueDelete = deletedFiles::add;
    private final TransactionType type;
    private TableMetadata base;
    private TableMetadata current;
    private boolean hasLastOpCommitted;
    private final MetricsReporter reporter;

    public KasanariMultiTableTransaction(
            String tableName, KasanariTableOperations ops, TransactionType type, TableMetadata start) {
        this(tableName, ops, type, start, LoggingMetricsReporter.instance());
    }

    public KasanariMultiTableTransaction(
            String tableName,
            KasanariTableOperations ops,
            TransactionType type,
            TableMetadata start,
            MetricsReporter reporter) {
        this.tableName = tableName;
        this.ops = ops;
        this.transactionTable = new TransactionTable();
        this.current = start;
        this.transactionOps = new TransactionTableOperations();
        this.updates = new ArrayList<>();
        this.base = ops.current();
        this.type = type;
        this.hasLastOpCommitted = true;
        this.reporter = reporter;
    }

    @Override
    public Table table() {
        return transactionTable;
    }

    public String tableName() {
        return tableName;
    }

    public TableMetadata startMetadata() {
        return base;
    }

    public TableMetadata currentMetadata() {
        return current;
    }

    public TableOperations underlyingOps() {
        return ops;
    }

    private void checkLastOperationCommitted(String operation) {
        if (!hasLastOpCommitted) {
            throw new IllegalStateException(String.format("Cannot create new %s: last operation has not committed", operation));
        }

        this.hasLastOpCommitted = false;
    }

    @Override
    public UpdateSchema updateSchema() {
        checkLastOperationCommitted("UpdateSchema");
        UpdateSchema schemaChange = new SchemaUpdate(transactionOps);
        updates.add(schemaChange);
        return schemaChange;
    }

    @Override
    public UpdatePartitionSpec updateSpec() {
        checkLastOperationCommitted("UpdateSpec");
        UpdatePartitionSpec partitionSpecChange = new BaseUpdatePartitionSpec(transactionOps);
        updates.add(partitionSpecChange);
        return partitionSpecChange;
    }

    @Override
    public UpdateProperties updateProperties() {
        checkLastOperationCommitted("UpdateProperties");
        UpdateProperties props = new PropertiesUpdate(transactionOps);
        updates.add(props);
        return props;
    }

    @Override
    public ReplaceSortOrder replaceSortOrder() {
        checkLastOperationCommitted("ReplaceSortOrder");
        ReplaceSortOrder replaceSortOrder = new BaseReplaceSortOrder(transactionOps);
        updates.add(replaceSortOrder);
        return replaceSortOrder;
    }

    @Override
    public UpdateLocation updateLocation() {
        checkLastOperationCommitted("UpdateLocation");
        UpdateLocation setLocation = new SetLocation(transactionOps);
        updates.add(setLocation);
        return setLocation;
    }

    @Override
    public AppendFiles newAppend() {
        checkLastOperationCommitted("AppendFiles");
        AppendFiles append = new MergeAppend(tableName, transactionOps).reportWith(reporter);
        append.deleteWith(enqueueDelete);
        updates.add(append);
        return append;
    }

    @Override
    public AppendFiles newFastAppend() {
        checkLastOperationCommitted("AppendFiles");
        AppendFiles append = new FastAppend(tableName, transactionOps).reportWith(reporter);
        updates.add(append);
        return append;
    }

    @Override
    public RewriteFiles newRewrite() {
        checkLastOperationCommitted("RewriteFiles");
        RewriteFiles rewrite = new BaseRewriteFiles(tableName, transactionOps).reportWith(reporter);
        rewrite.deleteWith(enqueueDelete);
        updates.add(rewrite);
        return rewrite;
    }

    @Override
    public RewriteManifests rewriteManifests() {
        checkLastOperationCommitted("RewriteManifests");
        RewriteManifests rewrite = new BaseRewriteManifests(tableName, transactionOps).reportWith(reporter);
        rewrite.deleteWith(enqueueDelete);
        updates.add(rewrite);
        return rewrite;
    }

    @Override
    public OverwriteFiles newOverwrite() {
        checkLastOperationCommitted("OverwriteFiles");
        OverwriteFiles overwrite =
                new BaseOverwriteFiles(tableName, transactionOps).reportWith(reporter);
        overwrite.deleteWith(enqueueDelete);
        updates.add(overwrite);
        return overwrite;
    }

    @Override
    public RowDelta newRowDelta() {
        checkLastOperationCommitted("RowDelta");
        RowDelta delta = new BaseRowDelta(tableName, transactionOps).reportWith(reporter);
        delta.deleteWith(enqueueDelete);
        updates.add(delta);
        return delta;
    }

    @Override
    public ReplacePartitions newReplacePartitions() {
        checkLastOperationCommitted("ReplacePartitions");
        ReplacePartitions replacePartitions =
                new BaseReplacePartitions(tableName, transactionOps).reportWith(reporter);
        replacePartitions.deleteWith(enqueueDelete);
        updates.add(replacePartitions);
        return replacePartitions;
    }

    @Override
    public DeleteFiles newDelete() {
        checkLastOperationCommitted("DeleteFiles");
        DeleteFiles delete = new StreamingDelete(tableName, transactionOps).reportWith(reporter);
        delete.deleteWith(enqueueDelete);
        updates.add(delete);
        return delete;
    }

    @Override
    public UpdateStatistics updateStatistics() {
        checkLastOperationCommitted("UpdateStatistics");
        UpdateStatistics updateStatistics = new SetStatistics(transactionOps);
        updates.add(updateStatistics);
        return updateStatistics;
    }

    @Override
    public UpdatePartitionStatistics updatePartitionStatistics() {
        checkLastOperationCommitted("UpdatePartitionStatistics");
        UpdatePartitionStatistics updatePartitionStatistics =
                new SetPartitionStatistics(transactionOps);
        updates.add(updatePartitionStatistics);
        return updatePartitionStatistics;
    }

    @Override
    public ExpireSnapshots expireSnapshots() {
        checkLastOperationCommitted("ExpireSnapshots");
        ExpireSnapshots expire = new RemoveSnapshots(transactionOps);
        expire.deleteWith(enqueueDelete);
        updates.add(expire);
        return expire;
    }

    @Override
    public ManageSnapshots manageSnapshots() {
        var snapshotManager = new KasanariSnapshotManager(this);
        updates.add(snapshotManager);
        return snapshotManager;
    }

    CherryPickOperation cherryPick() {
        checkLastOperationCommitted("CherryPick");
        CherryPickOperation cherrypick =
                new CherryPickOperation(tableName, transactionOps).reportWith(reporter);
        updates.add(cherrypick);
        return cherrypick;
    }

    SetSnapshotOperation setBranchSnapshot() {
        checkLastOperationCommitted("SetBranchSnapshot");
        SetSnapshotOperation set = new SetSnapshotOperation(transactionOps);
        updates.add(set);
        return set;
    }

    UpdateSnapshotReferencesOperation updateSnapshotReferencesOperation() {
        checkLastOperationCommitted("UpdateSnapshotReferencesOperation");
        UpdateSnapshotReferencesOperation manageSnapshotRefOperation =
                new UpdateSnapshotReferencesOperation(transactionOps);
        updates.add(manageSnapshotRefOperation);
        return manageSnapshotRefOperation;
    }

    @Override
    public void commitTransaction() {
        if (!hasLastOpCommitted) {
            throw new IllegalStateException("Cannot commit transaction: last operation has not committed");
        }

        switch (type) {
            case CREATE_TABLE:
                commitCreateTransaction();
                break;

            case REPLACE_TABLE:
                commitReplaceTransaction(false);
                break;

            case CREATE_OR_REPLACE_TABLE:
                commitReplaceTransaction(true);
                break;

            case SIMPLE:
                commitSimpleTransaction();
                break;
        }
    }

    private void commitCreateTransaction() {
        // this operation creates the table. if the commit fails, this cannot retry because another
        // process has created the same table.
        try {
            ops.commit(null, current);

        } catch (CommitStateUnknownException e) {
            throw e;

        } catch (RuntimeException e) {
            // the commit failed and no files were committed. clean up each update
            if (!ops.requireStrictCleanup() || e instanceof CleanableFailure) {
                cleanAllUpdates();
            }

            throw e;
        } finally {
            // create table never needs to retry because the table has no previous state. because retries
            // are not a
            // concern, it is safe to delete all of the deleted files from individual operations
            Tasks.foreach(deletedFiles)
                    .suppressFailureWhenFinished()
                    .onFailure((file, exc) -> logger.warn("Failed to delete uncommitted file: {}", file, exc))
                    .run(ops.io()::deleteFile);
        }
    }

    private void commitReplaceTransaction(boolean orCreate) {
        Map<String, String> props = base != null ? base.properties() : current.properties();

        try {
            Tasks.foreach(ops)
                    .retry(PropertyUtil.propertyAsInt(props, COMMIT_NUM_RETRIES, COMMIT_NUM_RETRIES_DEFAULT))
                    .exponentialBackoff(
                            PropertyUtil.propertyAsInt(
                                    props, COMMIT_MIN_RETRY_WAIT_MS, COMMIT_MIN_RETRY_WAIT_MS_DEFAULT),
                            PropertyUtil.propertyAsInt(
                                    props, COMMIT_MAX_RETRY_WAIT_MS, COMMIT_MAX_RETRY_WAIT_MS_DEFAULT),
                            PropertyUtil.propertyAsInt(
                                    props, COMMIT_TOTAL_RETRY_TIME_MS, COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT),
                            2.0 /* exponential */)
                    .onlyRetryOn(CommitFailedException.class)
                    .run(
                            underlyingOps -> {
                                try {
                                    underlyingOps.refresh();
                                } catch (NoSuchTableException e) {
                                    if (!orCreate) {
                                        throw e;
                                    }
                                }

                                // because this is a replace table, it will always completely replace the table
                                // metadata. even if it was just updated.
                                if (base != underlyingOps.current()) {
                                    this.base = underlyingOps.current(); // just refreshed
                                }

                                underlyingOps.commit(base, current);
                            });

        } catch (CommitStateUnknownException e) {
            throw e;

        } catch (RuntimeException e) {
            // the commit failed and no files were committed. clean up each update.
            if (!ops.requireStrictCleanup() || e instanceof CleanableFailure) {
                cleanAllUpdates();
            }

            throw e;

        } finally {
            // replace table never needs to retry because the table state is completely replaced. because
            // retries are not
            // a concern, it is safe to delete all of the deleted files from individual operations
            Tasks.foreach(deletedFiles)
                    .suppressFailureWhenFinished()
                    .onFailure((file, exc) -> logger.warn("Failed to delete uncommitted file: {}", file, exc))
                    .run(ops.io()::deleteFile);
        }
    }

    public void commitSimpleTransaction(Handle tx) {
        // if there were no changes, don't try to commit
        if (base == current) {
            return;
        }

        try {
            Tasks.foreach(ops)
                    .retry(base.propertyAsInt(COMMIT_NUM_RETRIES, COMMIT_NUM_RETRIES_DEFAULT))
                    .exponentialBackoff(
                            base.propertyAsInt(COMMIT_MIN_RETRY_WAIT_MS, COMMIT_MIN_RETRY_WAIT_MS_DEFAULT),
                            base.propertyAsInt(COMMIT_MAX_RETRY_WAIT_MS, COMMIT_MAX_RETRY_WAIT_MS_DEFAULT),
                            base.propertyAsInt(COMMIT_TOTAL_RETRY_TIME_MS, COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT),
                            2.0 /* exponential */)
                    .onlyRetryOn(CommitFailedException.class)
                    .run(
                            underlyingOps -> {
                                applyUpdates(underlyingOps);
                                underlyingOps.commit(tx, base, current);
                            });

        } catch (CommitStateUnknownException e) {
            throw e;
        } catch (PendingUpdateFailedException e) {
            cleanUpOnCommitFailure();
            throw e.wrapped();
        } catch (RuntimeException e) {
            if (!ops.requireStrictCleanup() || e instanceof CleanableFailure) {
                cleanUpOnCommitFailure();
            }
            throw e;
        }
    }

    public void cleanupAfterSimpleTransaction() {
        Set<Long> startingSnapshots =
                base.snapshots().stream().map(Snapshot::snapshotId).collect(Collectors.toSet());

        try {
            // clean up the data files that were deleted by each operation. first, get the list of
            // committed manifests to ensure that no committed manifest is deleted.
            // A manifest could be deleted in one successful operation commit, but reused in another
            // successful commit of that operation if the whole transaction is retried.
            Set<Long> newSnapshots = new HashSet<>();
            for (Snapshot snapshot : current.snapshots()) {
                if (!startingSnapshots.contains(snapshot.snapshotId())) {
                    newSnapshots.add(snapshot.snapshotId());
                }
            }

            Set<String> committedFiles = committedFiles(ops, newSnapshots);
            if (committedFiles != null) {
                // delete all of the files that were deleted in the most recent set of operation commits
                Tasks.foreach(deletedFiles)
                        .suppressFailureWhenFinished()
                        .onFailure((file, exc) -> logger.warn("Failed to delete uncommitted file: {}", file, exc))
                        .run(
                                path -> {
                                    if (!committedFiles.contains(path)) {
                                        ops.io().deleteFile(path);
                                    }
                                });
            } else {
                logger.warn("Failed to load metadata for a committed snapshot, skipping clean-up");
            }

        } catch (RuntimeException e) {
            logger.warn("Failed to load committed metadata, skipping clean-up", e);
        }
    }

    private void commitSimpleTransaction() {
        // if there were no changes, don't try to commit
        if (base == current) {
            return;
        }

        Set<Long> startingSnapshots =
                base.snapshots().stream().map(Snapshot::snapshotId).collect(Collectors.toSet());
        try {
            Tasks.foreach(ops)
                    .retry(base.propertyAsInt(COMMIT_NUM_RETRIES, COMMIT_NUM_RETRIES_DEFAULT))
                    .exponentialBackoff(
                            base.propertyAsInt(COMMIT_MIN_RETRY_WAIT_MS, COMMIT_MIN_RETRY_WAIT_MS_DEFAULT),
                            base.propertyAsInt(COMMIT_MAX_RETRY_WAIT_MS, COMMIT_MAX_RETRY_WAIT_MS_DEFAULT),
                            base.propertyAsInt(COMMIT_TOTAL_RETRY_TIME_MS, COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT),
                            2.0 /* exponential */)
                    .onlyRetryOn(CommitFailedException.class)
                    .run(
                            underlyingOps -> {
                                applyUpdates(underlyingOps);

                                underlyingOps.commit(base, current);
                            });

        } catch (CommitStateUnknownException e) {
            throw e;

        } catch (PendingUpdateFailedException e) {
            cleanUpOnCommitFailure();
            throw e.wrapped();
        } catch (RuntimeException e) {
            if (!ops.requireStrictCleanup() || e instanceof CleanableFailure) {
                cleanUpOnCommitFailure();
            }

            throw e;
        }

        // the commit succeeded

        try {
            // clean up the data files that were deleted by each operation. first, get the list of
            // committed manifests to ensure that no committed manifest is deleted.
            // A manifest could be deleted in one successful operation commit, but reused in another
            // successful commit of that operation if the whole transaction is retried.
            Set<Long> newSnapshots = new HashSet<>();
            for (Snapshot snapshot : current.snapshots()) {
                if (!startingSnapshots.contains(snapshot.snapshotId())) {
                    newSnapshots.add(snapshot.snapshotId());
                }
            }

            Set<String> committedFiles = committedFiles(ops, newSnapshots);
            if (committedFiles != null) {
                // delete all of the files that were deleted in the most recent set of operation commits
                Tasks.foreach(deletedFiles)
                        .suppressFailureWhenFinished()
                        .onFailure((file, exc) -> logger.warn("Failed to delete uncommitted file: {}", file, exc))
                        .run(
                                path -> {
                                    if (!committedFiles.contains(path)) {
                                        ops.io().deleteFile(path);
                                    }
                                });
            } else {
                logger.warn("Failed to load metadata for a committed snapshot, skipping clean-up");
            }

        } catch (RuntimeException e) {
            logger.warn("Failed to load committed metadata, skipping clean-up", e);
        }
    }

    private void cleanUpOnCommitFailure() {
        // the commit failed and no files were committed. clean up each update.
        cleanAllUpdates();

        // delete all the uncommitted files
        Tasks.foreach(deletedFiles)
                .suppressFailureWhenFinished()
                .onFailure((file, exc) -> logger.warn("Failed to delete uncommitted file: {}", file, exc))
                .run(ops.io()::deleteFile);
    }

    private void cleanAllUpdates() {
        Tasks.foreach(updates)
                .suppressFailureWhenFinished()
                .run(
                        update -> {
                            if (update instanceof SnapshotProducer) {
                                ((SnapshotProducer) update).cleanAll();
                            }
                        });
    }

    private void applyUpdates(TableOperations underlyingOps) {
        if (base != underlyingOps.refresh()) {
            // use refreshed the metadata
            this.base = underlyingOps.current();
            this.current = underlyingOps.current();
            for (PendingUpdate update : updates) {
                // re-commit each update in the chain to apply it and update current
                try {
                    update.commit();
                } catch (CommitFailedException e) {
                    // Cannot pass even with retry due to conflicting metadata changes. So, break the
                    // retry-loop.
                    throw new PendingUpdateFailedException(e);
                }
            }
        }
    }

    // committedFiles returns null whenever the set of committed files
    // cannot be determined from the provided snapshots
    private static Set<String> committedFiles(TableOperations ops, Set<Long> snapshotIds) {
        if (snapshotIds.isEmpty()) {
            return Set.of();
        }

        Set<String> committedFiles = new HashSet<>();

        for (long snapshotId : snapshotIds) {
            Snapshot snap = ops.current().snapshot(snapshotId);
            if (snap != null) {
                committedFiles.add(snap.manifestListLocation());
                snap.allManifests(ops.io()).forEach(manifest -> committedFiles.add(manifest.path()));
            } else {
                return null;
            }
        }

        return committedFiles;
    }

    public class TransactionTableOperations implements TableOperations {
        private TableOperations tempOps = ops.temp(current);

        @Override
        public TableMetadata current() {
            return current;
        }

        @Override
        public TableMetadata refresh() {
            return current;
        }

        @Override
        @SuppressWarnings("ConsistentOverrides")
        public void commit(TableMetadata underlyingBase, TableMetadata metadata) {
            if (underlyingBase != current) {
                // trigger a refresh and retry
                throw new CommitFailedException("Table metadata refresh is required");
            }

            KasanariMultiTableTransaction.this.current = metadata;

            this.tempOps = ops.temp(metadata);

            KasanariMultiTableTransaction.this.hasLastOpCommitted = true;
        }

        @Override
        public FileIO io() {
            return tempOps.io();
        }

        @Override
        public EncryptionManager encryption() {
            return tempOps.encryption();
        }

        @Override
        public String metadataFileLocation(String fileName) {
            return tempOps.metadataFileLocation(fileName);
        }

        @Override
        public LocationProvider locationProvider() {
            return tempOps.locationProvider();
        }

        @Override
        public long newSnapshotId() {
            return tempOps.newSnapshotId();
        }
    }

    public class TransactionTable implements Table, HasTableOperations, Serializable {

        @Override
        public TableOperations operations() {
            return transactionOps;
        }

        @Override
        public String name() {
            return tableName;
        }

        @Override
        public void refresh() {}

        @Override
        public TableScan newScan() {
            throw new UnsupportedOperationException("Transaction tables do not support scans");
        }

        @Override
        public Schema schema() {
            return current.schema();
        }

        @Override
        public Map<Integer, Schema> schemas() {
            return current.schemasById();
        }

        @Override
        public PartitionSpec spec() {
            return current.spec();
        }

        @Override
        public Map<Integer, PartitionSpec> specs() {
            return current.specsById();
        }

        @Override
        public SortOrder sortOrder() {
            return current.sortOrder();
        }

        @Override
        public Map<Integer, SortOrder> sortOrders() {
            return current.sortOrdersById();
        }

        @Override
        public Map<String, String> properties() {
            return current.properties();
        }

        @Override
        public String location() {
            return current.location();
        }

        @Override
        public Snapshot currentSnapshot() {
            return current.currentSnapshot();
        }

        @Override
        public Snapshot snapshot(long snapshotId) {
            return current.snapshot(snapshotId);
        }

        @Override
        public Iterable<Snapshot> snapshots() {
            return current.snapshots();
        }

        @Override
        public List<HistoryEntry> history() {
            return current.snapshotLog();
        }

        @Override
        public UpdateSchema updateSchema() {
            return KasanariMultiTableTransaction.this.updateSchema();
        }

        @Override
        public UpdatePartitionSpec updateSpec() {
            return KasanariMultiTableTransaction.this.updateSpec();
        }

        @Override
        public UpdateProperties updateProperties() {
            return KasanariMultiTableTransaction.this.updateProperties();
        }

        @Override
        public ReplaceSortOrder replaceSortOrder() {
            return KasanariMultiTableTransaction.this.replaceSortOrder();
        }

        @Override
        public UpdateLocation updateLocation() {
            return KasanariMultiTableTransaction.this.updateLocation();
        }

        @Override
        public AppendFiles newAppend() {
            return KasanariMultiTableTransaction.this.newAppend();
        }

        @Override
        public AppendFiles newFastAppend() {
            return KasanariMultiTableTransaction.this.newFastAppend();
        }

        @Override
        public RewriteFiles newRewrite() {
            return KasanariMultiTableTransaction.this.newRewrite();
        }

        @Override
        public RewriteManifests rewriteManifests() {
            return KasanariMultiTableTransaction.this.rewriteManifests();
        }

        @Override
        public OverwriteFiles newOverwrite() {
            return KasanariMultiTableTransaction.this.newOverwrite();
        }

        @Override
        public RowDelta newRowDelta() {
            return KasanariMultiTableTransaction.this.newRowDelta();
        }

        @Override
        public ReplacePartitions newReplacePartitions() {
            return KasanariMultiTableTransaction.this.newReplacePartitions();
        }

        @Override
        public DeleteFiles newDelete() {
            return KasanariMultiTableTransaction.this.newDelete();
        }

        @Override
        public UpdateStatistics updateStatistics() {
            return KasanariMultiTableTransaction.this.updateStatistics();
        }

        @Override
        public UpdatePartitionStatistics updatePartitionStatistics() {
            return KasanariMultiTableTransaction.this.updatePartitionStatistics();
        }

        @Override
        public ExpireSnapshots expireSnapshots() {
            return KasanariMultiTableTransaction.this.expireSnapshots();
        }

        @Override
        public ManageSnapshots manageSnapshots() {
            throw new UnsupportedOperationException(
                    "Transaction tables do not support managing snapshots");
        }

        @Override
        public Transaction newTransaction() {
            throw new UnsupportedOperationException("Cannot create a transaction within a transaction");
        }

        @Override
        public FileIO io() {
            return transactionOps.io();
        }

        @Override
        public EncryptionManager encryption() {
            return transactionOps.encryption();
        }

        @Override
        public LocationProvider locationProvider() {
            return transactionOps.locationProvider();
        }

        @Override
        public List<StatisticsFile> statisticsFiles() {
            return current.statisticsFiles();
        }

        @Override
        public List<PartitionStatisticsFile> partitionStatisticsFiles() {
            return current.partitionStatisticsFiles();
        }

        @Override
        public Map<String, SnapshotRef> refs() {
            return current.refs();
        }

        @Override
        public UUID uuid() {
            return UUID.fromString(current.uuid());
        }

        @Override
        public String toString() {
            return name();
        }

        Object writeReplace() {
            return SerializableTable.copyOf(this);
        }
    }

//    @VisibleForTesting
    TableOperations ops() {
        return ops;
    }

//    @VisibleForTesting
    Set<String> deletedFiles() {
        return deletedFiles;
    }

    /**
     * Exception used to avoid retrying {@link PendingUpdate} when it is failed with {@link
     * CommitFailedException}.
     */
    private static class PendingUpdateFailedException extends RuntimeException {
        private final CommitFailedException wrapped;

        private PendingUpdateFailedException(CommitFailedException cause) {
            super(cause);
            this.wrapped = cause;
        }

        public CommitFailedException wrapped() {
            return wrapped;
        }
    }
}
