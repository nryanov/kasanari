package kasanari.auth.ldap;

import io.smallrye.mutiny.Uni;
import kasanari.auth.spi.AuthCredentials;
import kasanari.auth.spi.AuthPrincipal;
import kasanari.auth.spi.AuthProvider;
import kasanari.auth.spi.AuthProviderContext;
import kasanari.auth.spi.AuthProviderMetadata;
import kasanari.auth.spi.AuthRequest;
import kasanari.auth.spi.CredentialScheme;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.evidence.PasswordGuessEvidence;

import java.util.Optional;

public final class LdapAuthProvider implements AuthProvider {
    private SecurityRealm securityRealm;

    @Override
    public String type() {
        return "ldap";
    }

    @Override
    public void initialize(AuthProviderContext context) {
        securityRealm = LdapSecurityRealms.create(
                context.getRequired("url"),
                context.getRequired("bind-principal"),
                context.getRequired("bind-password"),
                context.getRequired("search-base-dn"),
                context.getOptional("rdn-identifier").orElse("uid")
        );
    }

    @Override
    public AuthProviderMetadata metadata() {
        return AuthProviderMetadata.authenticated(CredentialScheme.BASIC);
    }

    @Override
    public Uni<Optional<AuthPrincipal>> authenticate(AuthRequest request) {
        return AuthCredentials.parseBasic(request)
                .map(credentials -> Uni.createFrom().item(authenticate(credentials)))
                .orElseGet(() -> Uni.createFrom().item(Optional.empty()));
    }

    private Optional<AuthPrincipal> authenticate(AuthCredentials.BasicCredentials credentials) {
        try {
            var identity = securityRealm.getRealmIdentity(new NamePrincipal(credentials.username()));
            if (!identity.exists()) {
                return Optional.empty();
            }

            var password = credentials.password().toCharArray();
            try {
                if (!identity.verifyEvidence(new PasswordGuessEvidence(password))) {
                    return Optional.empty();
                }
            } finally {
                java.util.Arrays.fill(password, '\0');
            }

            return Optional.of(new AuthPrincipal(credentials.username()));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
