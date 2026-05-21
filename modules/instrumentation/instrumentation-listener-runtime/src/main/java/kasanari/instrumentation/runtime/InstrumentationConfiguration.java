package kasanari.instrumentation.runtime;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "kasanari.instrumentation")
public interface InstrumentationConfiguration {
    @WithDefault("audit,logging")
    String listeners();
}
