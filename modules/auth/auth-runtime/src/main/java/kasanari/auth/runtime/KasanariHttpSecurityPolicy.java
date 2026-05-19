package kasanari.auth.runtime;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KasanariHttpSecurityPolicy implements HttpSecurityPolicy {
    private final AuthProviderRegistry registry;

    @Inject
    public KasanariHttpSecurityPolicy(AuthProviderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Uni<CheckResult> checkPermission(
            RoutingContext event,
            Uni<SecurityIdentity> identityUni,
            AuthorizationRequestContext requestContext) {
        if (!registry.metadata().authenticationRequired()) {
            return CheckResult.permit();
        }

        if (isPublicPath(event.normalizedPath())) {
            return CheckResult.permit();
        }

        return identityUni.onItem().transformToUni(identity -> {
            if (identity != null && !identity.isAnonymous()) {
                return CheckResult.permit();
            }
            return CheckResult.deny();
        });
    }

    @Override
    public String name() {
        return "kasanari-auth";
    }

    private boolean isPublicPath(String path) {
        for (var publicPath : registry.metadata().publicPaths()) {
            if (matchesPublicPath(path, publicPath)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesPublicPath(String path, String publicPath) {
        if ("/*".equals(publicPath)) {
            return true;
        }
        if (path.equals(publicPath)) {
            return true;
        }
        return path.startsWith(publicPath.endsWith("/") ? publicPath : publicPath + "/");
    }
}
