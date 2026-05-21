package kasanari.authentication.spi;

import io.smallrye.mutiny.Uni;

import java.util.Optional;

public interface AuthProvider {
    String type();

    void initialize(AuthProviderContext context);

    AuthProviderMetadata metadata();

    Uni<Optional<AuthPrincipal>> authenticate(AuthRequest request);
}
