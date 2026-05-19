package kasanari.auth.runtime;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import kasanari.auth.spi.AuthPrincipal;

import java.security.Principal;

final class SecurityIdentities {
    private SecurityIdentities() {
    }

    static Uni<SecurityIdentity> from(AuthPrincipal principal) {
        var builder = QuarkusSecurityIdentity.builder()
                .setPrincipal(new NamedPrincipal(principal.name()));

        principal.roles().forEach(builder::addRole);
        principal.attributes().forEach(builder::addAttribute);

        return Uni.createFrom().item(builder.build());
    }

    private record NamedPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
