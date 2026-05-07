package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.PartitionStateRecord;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.partition.PartitionStatistics;

import java.util.List;
import java.util.Map;

public interface PartitionStateRepository<T> {
    void persistCommitStatistics(
            T tx,
            Identifier identifier,
            String branch,
            long snapshotId,
            List<PartitionStatistics> statistics
    );

    List<PartitionStateRecord> findAll(T tx, Identifier identifier, String branch);

    List<PartitionStateRecord> findBySpecs(T tx, Identifier identifier, String branch, List<Map<String, String>> specs);

    void markDone(T tx, Identifier identifier, String branch, List<Map<String, String>> specs);

    void createPartitions(T tx, Identifier identifier, String branch, List<Map<String, String>> specs);

    void dropPartitions(T tx, Identifier identifier, String branch, List<Map<String, String>> specs);

    void alterPartitions(T tx, Identifier identifier, String branch, List<PartitionStatistics> partitions);
}
