package kasanari.catalog.iceberg;

import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.LocationProvider;

public class KasanariLocationProvider implements LocationProvider {
    @Override
    public String newDataLocation(String filename) {
        return "";
    }

    @Override
    public String newDataLocation(PartitionSpec spec, StructLike partitionData, String filename) {
        return "";
    }
}
