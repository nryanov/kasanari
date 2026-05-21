package kasanari.authorization.runtime;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "kasanari.authorization")
public interface AuthorizationConfiguration {
    @WithDefault("allow-all")
    String type();
}
