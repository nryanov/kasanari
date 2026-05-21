package kasanari.auth.runtime;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KasanariHttpAuthenticationMechanism implements HttpAuthenticationMechanism {
    private final AuthProviderRegistry registry;

    @Inject
    public KasanariHttpAuthenticationMechanism(AuthProviderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        if (!registry.metadata().authenticationRequired()) {
            return Uni.createFrom().nullItem();
        }

        return registry.activeProvider()
                .authenticate(AuthRequests.from(context))
                .flatMap(principal -> principal
                        .map(SecurityIdentities::from)
                        .orElseGet(() -> Uni.createFrom().nullItem()));
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return switch (registry.metadata().credentialScheme()) {
            case BASIC -> Uni.createFrom().item(new ChallengeData(401, "WWW-Authenticate", "Basic realm=\"kasanari\""));
            case BEARER -> Uni.createFrom().item(new ChallengeData(401, "WWW-Authenticate", "Bearer"));
            case NONE -> Uni.createFrom().nullItem();
        };
    }

    @Override
    public int getPriority() {
        return HttpAuthenticationMechanism.DEFAULT_PRIORITY + 100;
    }
}
