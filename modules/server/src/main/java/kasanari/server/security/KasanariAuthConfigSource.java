package kasanari.server.security;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public final class KasanariAuthConfigSource implements ConfigSource {
    private static final int ORDINAL = 275;

    private static final String AUTH_TYPE = "kasanari.auth.type";

    private static final String DISABLED_LDAP_URL = "ldap://127.0.0.1:1";
    private static final String DISABLED_LDAP_PRINCIPAL = "cn=disabled";
    private static final String DISABLED_LDAP_PASSWORD = "disabled";
    private static final String DISABLED_LDAP_SEARCH_BASE = "dc=disabled,dc=local";
    private static final String DISABLED_LDAP_RDN = "uid";

    private static final String DISABLED_OIDC_AUTH_SERVER_URL = "http://127.0.0.1:1/realms/disabled";
    private static final String DISABLED_OIDC_CLIENT_ID = "disabled";
    private static final String DISABLED_OIDC_SECRET = "disabled";

    private static final Set<String> MANAGED_PROPERTIES = Set.of(
            "quarkus.security.ldap.enabled",
            "quarkus.security.ldap.dir-context.url",
            "quarkus.security.ldap.dir-context.principal",
            "quarkus.security.ldap.dir-context.password",
            "quarkus.security.ldap.identity-mapping.search-base-dn",
            "quarkus.security.ldap.identity-mapping.rdn-identifier",
            "quarkus.oidc.tenant-enabled",
            "quarkus.oidc.application-type",
            "quarkus.oidc.auth-server-url",
            "quarkus.oidc.client-id",
            "quarkus.oidc.credentials.secret",
            "quarkus.http.auth.basic",
            "quarkus.http.auth.permission.public.paths",
            "quarkus.http.auth.permission.public.policy",
            "quarkus.http.auth.permission.public.enabled",
            "quarkus.http.auth.permission.authenticated.paths",
            "quarkus.http.auth.permission.authenticated.policy",
            "quarkus.http.auth.permission.authenticated.enabled"
    );

    @Override
    public Map<String, String> getProperties() {
        return Map.of();
    }

    @Override
    public Set<String> getPropertyNames() {
        return MANAGED_PROPERTIES;
    }

    @Override
    public String getValue(String propertyName) {
        if (!MANAGED_PROPERTIES.contains(propertyName)) {
            return null;
        }
        return valuesFor(resolveAuthType()).get(propertyName);
    }

    @Override
    public String getName() {
        return "KasanariAuthConfigSource";
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }

    private static AuthType resolveAuthType() {
        for (ConfigSource source : ConfigProvider.getConfig().getConfigSources()) {
            if (source instanceof KasanariAuthConfigSource) {
                continue;
            }

            var value = source.getValue(AUTH_TYPE);

            if (value != null && !value.isBlank()) {
                return AuthType.from(value.trim().toLowerCase(Locale.ROOT));
            }
        }
        return AuthType.NONE;
    }

    private static Map<String, String> valuesFor(AuthType authType) {
        var values = new HashMap<String, String>();
        return switch (authType) {
            case LDAP -> {
                values.put("quarkus.security.ldap.enabled", "true");
                values.put("quarkus.oidc.tenant-enabled", "false");
                values.put("quarkus.oidc.application-type", "service");
                values.put("quarkus.http.auth.basic", "true");
                yield withDisabledOidc(withHttpAuth(values, true));
            }
            case OAUTH -> {
                values.put("quarkus.security.ldap.enabled", "false");
                values.put("quarkus.oidc.tenant-enabled", "true");
                values.put("quarkus.oidc.application-type", "service");
                values.put("quarkus.http.auth.basic", "false");
                yield withDisabledLdap(withHttpAuth(values, true));
            }
            case NONE -> {
                values.put("quarkus.security.ldap.enabled", "false");
                values.put("quarkus.oidc.tenant-enabled", "false");
                values.put("quarkus.oidc.application-type", "service");
                values.put("quarkus.http.auth.basic", "false");
                yield withDisabledLdap(withDisabledOidc(withHttpAuth(values, false)));
            }
        };
    }

    private static Map<String, String> withDisabledLdap(Map<String, String> values) {
        values.put("quarkus.security.ldap.dir-context.url", DISABLED_LDAP_URL);
        values.put("quarkus.security.ldap.dir-context.principal", DISABLED_LDAP_PRINCIPAL);
        values.put("quarkus.security.ldap.dir-context.password", DISABLED_LDAP_PASSWORD);
        values.put("quarkus.security.ldap.identity-mapping.search-base-dn", DISABLED_LDAP_SEARCH_BASE);
        values.put("quarkus.security.ldap.identity-mapping.rdn-identifier", DISABLED_LDAP_RDN);
        return values;
    }

    private static Map<String, String> withDisabledOidc(Map<String, String> values) {
        values.put("quarkus.oidc.auth-server-url", DISABLED_OIDC_AUTH_SERVER_URL);
        values.put("quarkus.oidc.client-id", DISABLED_OIDC_CLIENT_ID);
        values.put("quarkus.oidc.credentials.secret", DISABLED_OIDC_SECRET);
        return values;
    }

    private static Map<String, String> withHttpAuth(Map<String, String> values, boolean requireAuthentication) {
        if (requireAuthentication) {
            values.put("quarkus.http.auth.permission.public.paths", "/q/health,/q/openapi,/docs");
            values.put("quarkus.http.auth.permission.public.policy", "permit");
            values.put("quarkus.http.auth.permission.public.enabled", "true");
            values.put("quarkus.http.auth.permission.authenticated.paths", "/*");
            values.put("quarkus.http.auth.permission.authenticated.policy", "authenticated");
            values.put("quarkus.http.auth.permission.authenticated.enabled", "true");
        } else {
            values.put("quarkus.http.auth.permission.public.paths", "/*");
            values.put("quarkus.http.auth.permission.public.policy", "permit");
            values.put("quarkus.http.auth.permission.public.enabled", "true");
            values.put("quarkus.http.auth.permission.authenticated.paths", "/*");
            values.put("quarkus.http.auth.permission.authenticated.policy", "authenticated");
            values.put("quarkus.http.auth.permission.authenticated.enabled", "false");
        }
        return values;
    }

    private enum AuthType {
        NONE,
        LDAP,
        OAUTH;

        static AuthType from(String value) {
            return switch (value) {
                case "ldap" -> LDAP;
                case "oauth" -> OAUTH;
                default -> NONE;
            };
        }
    }
}
