package kasanari.auth.oidc;

import io.smallrye.mutiny.Uni;
import kasanari.auth.spi.AuthCredentials;
import kasanari.auth.spi.AuthPrincipal;
import kasanari.auth.spi.AuthProvider;
import kasanari.auth.spi.AuthProviderContext;
import kasanari.auth.spi.AuthProviderMetadata;
import kasanari.auth.spi.AuthRequest;
import kasanari.auth.spi.CredentialScheme;

import java.util.Optional;

public final class OidcAuthProvider implements AuthProvider {
    private OidcTokenValidator tokenValidator;

    @Override
    public String type() {
        return "oidc";
    }

    @Override
    public void initialize(AuthProviderContext context) {
        tokenValidator = OidcTokenValidator.create(
                context.getRequired("issuer-url"),
                context.getOptional("client-id")
        );
    }

    @Override
    public AuthProviderMetadata metadata() {
        return AuthProviderMetadata.authenticated(CredentialScheme.BEARER);
    }

    @Override
    public Uni<Optional<AuthPrincipal>> authenticate(AuthRequest request) {
        return AuthCredentials.parseBearer(request)
                .map(token -> Uni.createFrom().item(
                        tokenValidator.validate(token).map(AuthPrincipal::new)))
                .orElseGet(() -> Uni.createFrom().item(Optional.empty()));
    }
}
