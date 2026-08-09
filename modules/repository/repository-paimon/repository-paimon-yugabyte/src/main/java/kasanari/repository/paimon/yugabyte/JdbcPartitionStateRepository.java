package kasanari.repository.paimon.yugabyte;

import kasanari.repository.paimon.model.PartitionStateRecord;
import kasanari.repository.paimon.PartitionStateRepository;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.partition.PartitionStatistics;
import org.jdbi.v3.core.Handle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdbcPartitionStateRepository implements PartitionStateRepository<Handle> {
    private static final String EMPTY_JSON = "{}";

    private final String catalogKey;

    public JdbcPartitionStateRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public void persistCommitStatistics(
            Handle tx,
            Identifier identifier,
            String branch,
            long snapshotId,
            List<PartitionStatistics> statistics
    ) {
        if (statistics == null || statistics.isEmpty()) {
            return;
        }

        for (var statistic : statistics) {
            var spec = normalizeSpec(statistic.spec());
            var specHash = JsonSerde.hashSpec(spec);

            var insertDelta = tx.createUpdate(JdbcQueries.INSERT_PARTITION_STATS_DELTA);
            insertDelta.bind(0, catalogKey);
            insertDelta.bind(1, identifier.getDatabaseName());
            insertDelta.bind(2, identifier.getTableName());
            insertDelta.bind(3, branch);
            insertDelta.bind(4, snapshotId);
            insertDelta.bind(5, specHash);
            insertDelta.bind(6, JsonSerde.encodeSpec(spec));
            insertDelta.bind(7, statistic.recordCount());
            insertDelta.bind(8, statistic.fileSizeInBytes());
            insertDelta.bind(9, statistic.fileCount());
            insertDelta.bind(10, statistic.lastFileCreationTime());
            insertDelta.bind(11, statistic.totalBuckets());
            insertDelta.execute();

            var upsertState = tx.createUpdate(JdbcQueries.UPSERT_PARTITION_STATE);
            upsertState.bind(0, catalogKey);
            upsertState.bind(1, identifier.getDatabaseName());
            upsertState.bind(2, identifier.getTableName());
            upsertState.bind(3, branch);
            upsertState.bind(4, specHash);
            upsertState.bind(5, JsonSerde.encodeSpec(spec));
            upsertState.bind(6, statistic.recordCount());
            upsertState.bind(7, statistic.fileSizeInBytes());
            upsertState.bind(8, statistic.fileCount());
            upsertState.bind(9, statistic.lastFileCreationTime());
            upsertState.bind(10, statistic.totalBuckets());
            upsertState.bind(11, EMPTY_JSON);
            upsertState.execute();
        }

        var cleanup = tx.createUpdate(JdbcQueries.DELETE_EMPTY_PARTITION_STATES);
        cleanup.bind(0, catalogKey);
        cleanup.bind(1, identifier.getDatabaseName());
        cleanup.bind(2, identifier.getTableName());
        cleanup.bind(3, branch);
        cleanup.execute();
    }

    @Override
    public List<PartitionStateRecord> findAll(Handle tx, Identifier identifier, String branch) {
        var query = tx.createQuery(JdbcQueries.LIST_PARTITION_STATES);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, branch);
        return query.map((rs, ctx) -> new PartitionStateRecord(
                JsonSerde.decodeSpec(rs.getString("spec_payload")),
                rs.getLong("record_count"),
                rs.getLong("file_size_in_bytes"),
                rs.getLong("file_count"),
                rs.getLong("last_file_creation_time"),
                rs.getInt("total_buckets"),
                rs.getBoolean("done"),
                JsonSerde.decodeMap(rs.getString("options_payload")),
                0L
        )).list();
    }

    @Override
    public List<PartitionStateRecord> findPage(
            Handle tx,
            Identifier identifier,
            String branch,
            long idAfter,
            int pageSize) {
        var query = tx.createQuery(JdbcQueries.LIST_PARTITION_STATES_PAGE);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, branch);
        query.bind(4, idAfter);
        query.bind(5, pageSize);
        return query.map((rs, ctx) -> new PartitionStateRecord(
                JsonSerde.decodeSpec(rs.getString("spec_payload")),
                rs.getLong("record_count"),
                rs.getLong("file_size_in_bytes"),
                rs.getLong("file_count"),
                rs.getLong("last_file_creation_time"),
                rs.getInt("total_buckets"),
                rs.getBoolean("done"),
                JsonSerde.decodeMap(rs.getString("options_payload")),
                rs.getLong("id")
        )).list();
    }

    @Override
    public List<PartitionStateRecord> findBySpecs(
            Handle tx,
            Identifier identifier,
            String branch,
            List<Map<String, String>> specs
    ) {
        if (specs == null || specs.isEmpty()) {
            return List.of();
        }

        var hashes =
                specs.stream()
                        .map(JdbcPartitionStateRepository::normalizeSpec)
                        .map(JsonSerde::hashSpec)
                        .collect(Collectors.toSet());

        var all = findAll(tx, identifier, branch);
        var filtered = new ArrayList<PartitionStateRecord>();
        for (var state : all) {
            var hash = JsonSerde.hashSpec(state.spec());
            if (hashes.contains(hash)) {
                filtered.add(state);
            }
        }
        return filtered;
    }

    @Override
    public void markDone(Handle tx, Identifier identifier, String branch, List<Map<String, String>> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        var uniqueHashes = specs.stream()
                .map(JdbcPartitionStateRepository::normalizeSpec)
                .map(JsonSerde::hashSpec)
                .collect(Collectors.toSet());
        for (var specHash : uniqueHashes) {
            var query = tx.createUpdate(JdbcQueries.MARK_DONE_PARTITION_STATE);
            query.bind(0, catalogKey);
            query.bind(1, identifier.getDatabaseName());
            query.bind(2, identifier.getTableName());
            query.bind(3, branch);
            query.bind(4, specHash);
            query.execute();
        }
    }

    @Override
    public void createPartitions(Handle tx, Identifier identifier, String branch, List<Map<String, String>> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        for (var spec : specs) {
            var normalizedSpec = normalizeSpec(spec);
            var specHash = JsonSerde.hashSpec(normalizedSpec);

            var query = tx.createUpdate(JdbcQueries.INSERT_PARTITION_STATE_IF_ABSENT);
            query.bind(0, catalogKey);
            query.bind(1, identifier.getDatabaseName());
            query.bind(2, identifier.getTableName());
            query.bind(3, branch);
            query.bind(4, specHash);
            query.bind(5, JsonSerde.encodeSpec(normalizedSpec));
            query.execute();
        }
    }

    @Override
    public void dropPartitions(Handle tx, Identifier identifier, String branch, List<Map<String, String>> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        for (var spec : specs) {
            var specHash = JsonSerde.hashSpec(normalizeSpec(spec));
            var query = tx.createUpdate(JdbcQueries.DELETE_PARTITION_STATE_BY_HASH);
            query.bind(0, catalogKey);
            query.bind(1, identifier.getDatabaseName());
            query.bind(2, identifier.getTableName());
            query.bind(3, branch);
            query.bind(4, specHash);
            query.execute();
        }
    }

    @Override
    public void alterPartitions(
            Handle tx,
            Identifier identifier,
            String branch,
            List<PartitionStatistics> partitions
    ) {
        if (partitions == null || partitions.isEmpty()) {
            return;
        }

        for (var partition : partitions) {
            var normalizedSpec = normalizeSpec(partition.spec());
            var specHash = JsonSerde.hashSpec(normalizedSpec);
            var query = tx.createUpdate(JdbcQueries.UPSERT_PARTITION_STATE_ABSOLUTE);
            query.bind(0, catalogKey);
            query.bind(1, identifier.getDatabaseName());
            query.bind(2, identifier.getTableName());
            query.bind(3, branch);
            query.bind(4, specHash);
            query.bind(5, JsonSerde.encodeSpec(normalizedSpec));
            query.bind(6, partition.recordCount());
            query.bind(7, partition.fileSizeInBytes());
            query.bind(8, partition.fileCount());
            query.bind(9, partition.lastFileCreationTime());
            query.bind(10, partition.totalBuckets());
            query.execute();
        }
    }

    private static Map<String, String> normalizeSpec(Map<?, ?> spec) {
        if (spec == null || spec.isEmpty()) {
            return Map.of();
        }

        var normalized = new LinkedHashMap<String, String>();
        for (var entry : spec.entrySet()) {
            normalized.put(String.valueOf(entry.getKey()), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        return normalized;
    }
}
