package kasanari.server.configuration;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = "kasanari.management.metadata")
public interface ManagementMetadataConfiguration {
    Map<String, String> jdbcProperties();
}
