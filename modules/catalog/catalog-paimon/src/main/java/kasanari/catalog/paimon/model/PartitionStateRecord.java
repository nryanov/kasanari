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
        Map<String, String> options,
        long id
) {
    public PartitionStateRecord(
            Map<String, String> spec,
            long recordCount,
            long fileSizeInBytes,
            long fileCount,
            long lastFileCreationTime,
            int totalBuckets,
            boolean done,
            Map<String, String> options
    ) {
        this(spec, recordCount, fileSizeInBytes, fileCount, lastFileCreationTime, totalBuckets, done, options, 0L);
    }

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
