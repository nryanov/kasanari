package kasanari.authentication.runtime;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "kasanari.authentication")
public interface AuthConfiguration {
    @WithDefault("none")
    String type();
}
