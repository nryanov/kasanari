package kasanari.authentication.oidc.internal;

import io.quarkus.oidc.common.runtime.config.OidcCommonConfig;

import java.util.Optional;

record SimpleOidcProxy(Optional<String> proxyConfigurationName, Optional<String> host, int port,
                       Optional<String> username, Optional<String> password) implements OidcCommonConfig.Proxy {

    static SimpleOidcProxy empty() {
        return new SimpleOidcProxy(Optional.empty(), Optional.empty(), 80, Optional.empty(), Optional.empty());
    }

    @Override
    @SuppressWarnings("removal")
    public Optional<String> host() {
        return host;
    }

    @Override
    @SuppressWarnings("removal")
    public int port() {
        return port;
    }

    @Override
    @SuppressWarnings("removal")
    public Optional<String> username() {
        return username;
    }

    @Override
    @SuppressWarnings("removal")
    public Optional<String> password() {
        return password;
    }
}
