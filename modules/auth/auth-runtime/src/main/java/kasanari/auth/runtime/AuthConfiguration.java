package kasanari.auth.runtime;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "kasanari.auth")
public interface AuthConfiguration {
    @WithDefault("none")
    String type();
}
