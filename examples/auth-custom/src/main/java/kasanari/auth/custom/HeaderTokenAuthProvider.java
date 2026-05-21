package kasanari.authentication.custom;

import io.smallrye.mutiny.Uni;
import kasanari.authentication.spi.AuthPrincipal;
import kasanari.authentication.spi.AuthProvider;
import kasanari.authentication.spi.AuthProviderContext;
import kasanari.authentication.spi.AuthProviderMetadata;
import kasanari.authentication.spi.AuthRequest;
import kasanari.authentication.spi.CredentialScheme;

import java.util.Optional;

public final class HeaderTokenAuthProvider implements AuthProvider {
    private String headerName;
    private String secret;

    @Override
    public String type() {
        return "header-token";
    }

    @Override
    public void initialize(AuthProviderContext context) {
        headerName = context.getOptional("header").orElse("X-Kasanari-Token");
        secret = context.getRequired("secret");
    }

    @Override
    public AuthProviderMetadata metadata() {
        return new AuthProviderMetadata(true, CredentialScheme.NONE, AuthProviderMetadata.DEFAULT_PUBLIC_PATHS);
    }

    @Override
    public Uni<Optional<AuthPrincipal>> authenticate(AuthRequest request) {
        return request.header(headerName)
                .filter(value -> value.equals(secret))
                .map(value -> Uni.createFrom().item(Optional.of(new AuthPrincipal("header-token-user"))))
                .orElseGet(() -> Uni.createFrom().item(Optional.empty()));
    }
}
