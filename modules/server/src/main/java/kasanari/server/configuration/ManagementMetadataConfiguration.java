package kasanari.server.configuration;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = "management.metadata")
public interface ManagementMetadataConfiguration {
    Map<String, String> jdbcProperties();
}
