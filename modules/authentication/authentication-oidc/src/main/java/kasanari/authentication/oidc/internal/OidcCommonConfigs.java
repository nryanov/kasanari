package kasanari.authentication.oidc.internal;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.oidc.common.runtime.OidcTlsSupport;
import io.quarkus.oidc.common.runtime.config.OidcCommonConfig;
import io.quarkus.proxy.ProxyConfigurationRegistry;
import io.quarkus.tls.TlsConfigurationRegistry;
import kasanari.authentication.spi.AuthProviderContext;

import java.time.Duration;
import java.util.Optional;

final class OidcCommonConfigs {
    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final int DEFAULT_CONNECTION_RETRY_COUNT = 3;
    private static final int DEFAULT_CLOCK_SKEW_SECONDS = 30;
    private static final String DEFAULT_DISCOVERY_PATH = ".well-known/openid-configuration";
    private static final ProxyConfigurationRegistry NO_PROXY = name -> Optional.empty();

    private OidcCommonConfigs() {
    }

    record Built(
            OidcCommonConfig config,
            OidcTlsSupport tlsSupport,
            ProxyConfigurationRegistry proxyRegistry,
            String issuerUrl,
            Optional<String> clientId,
            int clockSkewSeconds
    ) {
    }

    static Built from(AuthProviderContext context) {
        var issuerUrl = trimTrailingSlash(context.getRequired("issuer-url"));
        var clientId = OidcConfigParsers.optionalString(context, "client-id");
        var clockSkewSeconds = OidcConfigParsers.intOrDefault(
                context, "clock-skew-seconds", DEFAULT_CLOCK_SKEW_SECONDS);

        var tls = buildTls(context);
        var proxy = buildProxy(context);
        var config = new SimpleOidcCommonConfig(
                Optional.of(issuerUrl),
                OidcConfigParsers.optionalBoolean(context, "discovery-enabled").or(() -> Optional.of(true)),
                OidcConfigParsers.stringOrDefault(context, "discovery-path", DEFAULT_DISCOVERY_PATH),
                OidcConfigParsers.optionalString(context, "registration-path"),
                OidcConfigParsers.optionalDuration(context, "connection-delay"),
                OidcConfigParsers.intOrDefault(context, "connection-retry-count", DEFAULT_CONNECTION_RETRY_COUNT),
                OidcConfigParsers.durationOrDefault(context, "connection-timeout", DEFAULT_CONNECTION_TIMEOUT),
                OidcConfigParsers.booleanOrDefault(context, "use-blocking-dns-lookup", false),
                OidcConfigParsers.optionalInt(context, "max-pool-size"),
                OidcConfigParsers.booleanOrDefault(context, "follow-redirects", true),
                proxy,
                tls
        );

        return new Built(
                config,
                resolveTlsSupport(tls),
                resolveProxyRegistry(proxy),
                issuerUrl,
                clientId,
                clockSkewSeconds
        );
    }

    private static SimpleOidcTls buildTls(AuthProviderContext context) {
        return new SimpleOidcTls(
                OidcConfigParsers.optionalString(context, "tls-configuration-name"),
                OidcConfigParsers.optionalEnum(
                        context, "tls-verification", OidcCommonConfig.Tls.Verification.class),
                OidcConfigParsers.optionalPath(context, "key-store-file"),
                OidcConfigParsers.optionalString(context, "key-store-file-type"),
                OidcConfigParsers.optionalString(context, "key-store-provider"),
                OidcConfigParsers.optionalString(context, "key-store-password"),
                OidcConfigParsers.optionalString(context, "key-store-key-alias"),
                OidcConfigParsers.optionalString(context, "key-store-key-password"),
                OidcConfigParsers.optionalPath(context, "trust-store-file"),
                OidcConfigParsers.optionalString(context, "trust-store-password"),
                OidcConfigParsers.optionalString(context, "trust-store-cert-alias"),
                OidcConfigParsers.optionalString(context, "trust-store-file-type"),
                OidcConfigParsers.optionalString(context, "trust-store-provider")
        );
    }

    private static SimpleOidcProxy buildProxy(AuthProviderContext context) {
        var host = OidcConfigParsers.optionalString(context, "proxy-host");
        var port = OidcConfigParsers.intOrDefault(context, "proxy-port", 80);
        return new SimpleOidcProxy(
                OidcConfigParsers.optionalString(context, "proxy-configuration-name"),
                host,
                port,
                OidcConfigParsers.optionalString(context, "proxy-username"),
                OidcConfigParsers.optionalString(context, "proxy-password")
        );
    }

    private static OidcTlsSupport resolveTlsSupport(SimpleOidcTls tls) {
        if (tls.tlsConfigurationName().isEmpty()) {
            return OidcTlsSupport.empty();
        }
        return resolveBean(TlsConfigurationRegistry.class)
                .map(OidcTlsSupport::of)
                .orElseThrow(() -> new IllegalStateException(
                        "kasanari.authentication.oidc.tls-configuration-name is set but "
                                + "TlsConfigurationRegistry is not available"));
    }

    private static ProxyConfigurationRegistry resolveProxyRegistry(SimpleOidcProxy proxy) {
        if (proxy.proxyConfigurationName().isEmpty()) {
            return NO_PROXY;
        }
        return resolveBean(ProxyConfigurationRegistry.class)
                .orElseThrow(() -> new IllegalStateException(
                        "kasanari.authentication.oidc.proxy-configuration-name is set but "
                                + "ProxyConfigurationRegistry is not available"));
    }

    private static <T> Optional<T> resolveBean(Class<T> type) {
        ArcContainer container = Arc.container();
        if (container == null) {
            return Optional.empty();
        }
        InstanceHandle<T> instance = container.instance(type);
        if (!instance.isAvailable()) {
            return Optional.empty();
        }
        return Optional.of(instance.get());
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
