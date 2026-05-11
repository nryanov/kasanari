package kasanari.server.configuration;

import io.smallrye.config.ConfigMapping;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import java.util.Map;

@ConfigMapping(prefix = "catalog.iceberg")
public interface IcebergCatalogConfiguration {
    IcebergCatalogType type();

    String name();

    Map<String, String> catalogProperties();

    Map<String, String> hadoopProperties();

    enum IcebergCatalogType {
        PROXY, KASANARI
    }
}
