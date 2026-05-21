package kasanari.authorization.allowall;

import kasanari.authorization.spi.AuthorizationProvider;
import kasanari.authorization.spi.AuthorizationProviderContext;
import kasanari.authorization.spi.AuthorizationRequest;

public final class AllowAllAuthorizationProvider implements AuthorizationProvider {
    @Override
    public String type() {
        return "allow-all";
    }

    @Override
    public void initialize(AuthorizationProviderContext context) {
    }

    @Override
    public boolean isAuthorized(AuthorizationRequest request) {
        return true;
    }
}
