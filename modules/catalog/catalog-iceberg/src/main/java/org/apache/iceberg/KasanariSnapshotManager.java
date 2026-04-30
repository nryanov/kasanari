package org.apache.iceberg;


import kasanari.catalog.iceberg.operations.KasanariTableOperations;

// copy of SnapshotManager
public class KasanariSnapshotManager implements ManageSnapshots {
    private final boolean isExternalTransaction;
    private final KasanariMultiTableTransaction transaction;
    private UpdateSnapshotReferencesOperation updateSnapshotReferencesOperation;

    KasanariSnapshotManager(String tableName, KasanariTableOperations ops) {
        if (ops.current() == null) {
            throw new IllegalStateException(String.format("Cannot manage snapshots: table %s does not exist", tableName));
        }

        this.transaction =
                new KasanariMultiTableTransaction(tableName, ops, KasanariMultiTableTransaction.TransactionType.SIMPLE, ops.refresh());
        this.isExternalTransaction = false;
    }

    KasanariSnapshotManager(KasanariMultiTableTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Invalid input transaction: null");
        }

        this.transaction = transaction;
        this.isExternalTransaction = true;
    }

    @Override
    public ManageSnapshots cherrypick(long snapshotId) {
        commitIfRefUpdatesExist();
        transaction.cherryPick().cherrypick(snapshotId).commit();
        return this;
    }

    @Override
    public ManageSnapshots setCurrentSnapshot(long snapshotId) {
        commitIfRefUpdatesExist();
        transaction.setBranchSnapshot().setCurrentSnapshot(snapshotId).commit();
        return this;
    }

    @Override
    public ManageSnapshots rollbackToTime(long timestampMillis) {
        commitIfRefUpdatesExist();
        transaction.setBranchSnapshot().rollbackToTime(timestampMillis).commit();
        return this;
    }

    @Override
    public ManageSnapshots rollbackTo(long snapshotId) {
        commitIfRefUpdatesExist();
        transaction.setBranchSnapshot().rollbackTo(snapshotId).commit();
        return this;
    }

    @Override
    public ManageSnapshots createBranch(String name) {
        Snapshot currentSnapshot = transaction.currentMetadata().currentSnapshot();
        if (currentSnapshot != null) {
            return createBranch(name, currentSnapshot.snapshotId());
        }

        SnapshotRef existingRef = transaction.currentMetadata().ref(name);

        if (existingRef != null) {
            throw new IllegalArgumentException(String.format("Ref %s already exists", name));
        }

        // Create an empty snapshot for the branch
        transaction.newFastAppend().toBranch(name).commit();
        return this;
    }

    @Override
    public ManageSnapshots createBranch(String name, long snapshotId) {
        updateSnapshotReferencesOperation().createBranch(name, snapshotId);
        return this;
    }

    @Override
    public ManageSnapshots createTag(String name, long snapshotId) {
        updateSnapshotReferencesOperation().createTag(name, snapshotId);
        return this;
    }

    @Override
    public ManageSnapshots removeBranch(String name) {
        updateSnapshotReferencesOperation().removeBranch(name);
        return this;
    }

    @Override
    public ManageSnapshots removeTag(String name) {
        updateSnapshotReferencesOperation().removeTag(name);
        return this;
    }

    @Override
    public ManageSnapshots setMinSnapshotsToKeep(String name, int minSnapshotsToKeep) {
        updateSnapshotReferencesOperation().setMinSnapshotsToKeep(name, minSnapshotsToKeep);
        return this;
    }

    @Override
    public ManageSnapshots setMaxSnapshotAgeMs(String name, long maxSnapshotAgeMs) {
        updateSnapshotReferencesOperation().setMaxSnapshotAgeMs(name, maxSnapshotAgeMs);
        return this;
    }

    @Override
    public ManageSnapshots setMaxRefAgeMs(String name, long maxRefAgeMs) {
        updateSnapshotReferencesOperation().setMaxRefAgeMs(name, maxRefAgeMs);
        return this;
    }

    @Override
    public ManageSnapshots replaceTag(String name, long snapshotId) {
        updateSnapshotReferencesOperation().replaceTag(name, snapshotId);
        return this;
    }

    @Override
    public ManageSnapshots replaceBranch(String name, long snapshotId) {
        updateSnapshotReferencesOperation().replaceBranch(name, snapshotId);
        return this;
    }

    @Override
    public ManageSnapshots replaceBranch(String from, String to) {
        updateSnapshotReferencesOperation().replaceBranch(from, to);
        return this;
    }

    @Override
    public ManageSnapshots fastForwardBranch(String from, String to) {
        updateSnapshotReferencesOperation().fastForward(from, to);
        return this;
    }

    @Override
    public ManageSnapshots renameBranch(String name, String newName) {
        updateSnapshotReferencesOperation().renameBranch(name, newName);
        return this;
    }

    private UpdateSnapshotReferencesOperation updateSnapshotReferencesOperation() {
        if (updateSnapshotReferencesOperation == null) {
            this.updateSnapshotReferencesOperation = transaction.updateSnapshotReferencesOperation();
        }

        return updateSnapshotReferencesOperation;
    }

    private void commitIfRefUpdatesExist() {
        if (updateSnapshotReferencesOperation != null) {
            updateSnapshotReferencesOperation.commit();
            updateSnapshotReferencesOperation = null;
        }
    }

    @Override
    public Snapshot apply() {
        return transaction.table().currentSnapshot();
    }

    @Override
    public void commit() {
        commitIfRefUpdatesExist();
        if (!isExternalTransaction) {
            transaction.commitTransaction();
        }
    }
}
