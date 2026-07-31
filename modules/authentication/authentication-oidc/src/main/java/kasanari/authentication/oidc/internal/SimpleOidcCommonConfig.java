package kasanari.authentication.oidc.internal;

import io.quarkus.oidc.common.runtime.config.OidcCommonConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;

record SimpleOidcCommonConfig(Optional<String> authServerUrl, Optional<Boolean> discoveryEnabled, String discoveryPath,
                              Optional<String> registrationPath, Optional<Duration> connectionDelay,
                              int connectionRetryCount, Duration connectionTimeout, boolean useBlockingDnsLookup,
                              OptionalInt maxPoolSize, boolean followRedirects, Proxy proxy,
                              Tls tls) implements OidcCommonConfig {
}
