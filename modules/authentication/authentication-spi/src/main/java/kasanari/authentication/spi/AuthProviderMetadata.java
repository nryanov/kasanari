package kasanari.authentication.spi;

import java.util.Set;

public record AuthProviderMetadata(
        boolean authenticationRequired,
        CredentialScheme credentialScheme,
        Set<String> publicPaths
) {
    public static final Set<String> DEFAULT_PUBLIC_PATHS = Set.of("/q/health", "/q/openapi", "/docs");

    public static AuthProviderMetadata none() {
        return new AuthProviderMetadata(false, CredentialScheme.NONE, Set.of("/*"));
    }

    public static AuthProviderMetadata authenticated(CredentialScheme scheme) {
        return new AuthProviderMetadata(true, scheme, DEFAULT_PUBLIC_PATHS);
    }
}
