package kasanari.catalog.paimon.model;

import org.apache.paimon.partition.Partition;

import java.util.Map;

public record PartitionStateRecord(
        Map<String, String> spec,
        long recordCount,
        long fileSizeInBytes,
        long fileCount,
        long lastFileCreationTime,
        int totalBuckets,
        boolean done,
        Map<String, String> options
) {
    public Partition toPartition() {
        return new Partition(
                spec,
                recordCount,
                fileSizeInBytes,
                fileCount,
                lastFileCreationTime,
                totalBuckets,
                done,
                null,
                null,
                null,
                null,
                options
        );
    }
}
