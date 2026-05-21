package kasanari.authorization.spi;

import java.util.Optional;

public interface AuthorizationProvider {
    String type();

    void initialize(AuthorizationProviderContext context);

    boolean isAuthorized(AuthorizationRequest request);

    default Optional<RoleBindingAdministration> roleBindings() {
        return Optional.empty();
    }
}
