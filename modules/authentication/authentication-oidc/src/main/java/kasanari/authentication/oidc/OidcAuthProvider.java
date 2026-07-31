package kasanari.authentication.oidc;

import io.smallrye.mutiny.Uni;
import kasanari.authentication.oidc.internal.OidcTokenValidator;
import kasanari.authentication.spi.AuthCredentials;
import kasanari.authentication.spi.AuthPrincipal;
import kasanari.authentication.spi.AuthProvider;
import kasanari.authentication.spi.AuthProviderContext;
import kasanari.authentication.spi.AuthProviderMetadata;
import kasanari.authentication.spi.AuthRequest;
import kasanari.authentication.spi.CredentialScheme;

import java.util.Optional;

public final class OidcAuthProvider implements AuthProvider {
    private OidcTokenValidator tokenValidator;

    @Override
    public String type() {
        return "oidc";
    }

    @Override
    public void initialize(AuthProviderContext context) {
        tokenValidator = OidcTokenValidator.create(context);
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
