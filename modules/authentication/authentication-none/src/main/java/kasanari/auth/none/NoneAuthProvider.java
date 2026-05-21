package kasanari.auth.none;

import io.smallrye.mutiny.Uni;
import kasanari.auth.spi.AuthPrincipal;
import kasanari.auth.spi.AuthProvider;
import kasanari.auth.spi.AuthProviderContext;
import kasanari.auth.spi.AuthProviderMetadata;
import kasanari.auth.spi.AuthRequest;

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
