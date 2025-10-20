package kasanari.server.configuration;

import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import java.util.Map;

@ConfigMapping(prefix = "catalog.iceberg")
public interface IcebergCatalogConfiguration {
    IcebergCatalogType type();

    Map<String, String> properties();

    enum IcebergCatalogType {
        IN_MEMORY, HADOOP, HIVE, JDBC, NESSIE, REST, KASANARI
    }
}
