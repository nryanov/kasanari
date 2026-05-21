package kasanari.authentication.none;

import io.smallrye.mutiny.Uni;
import kasanari.authentication.spi.AuthPrincipal;
import kasanari.authentication.spi.AuthProvider;
import kasanari.authentication.spi.AuthProviderContext;
import kasanari.authentication.spi.AuthProviderMetadata;
import kasanari.authentication.spi.AuthRequest;

import java.util.Optional;

public final class NoneAuthProvider implements AuthProvider {
    @Override
    public String type() {
        return "none";
    }

    @Override
    public void initialize(AuthProviderContext context) {
    }

    @Override
    public AuthProviderMetadata metadata() {
        return AuthProviderMetadata.none();
    }

    @Override
    public Uni<Optional<AuthPrincipal>> authenticate(AuthRequest request) {
        return Uni.createFrom().item(Optional.empty());
    }
}
