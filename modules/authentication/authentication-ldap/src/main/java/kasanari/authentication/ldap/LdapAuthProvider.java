package kasanari.authentication.ldap;

import io.smallrye.mutiny.Uni;
import kasanari.authentication.spi.AuthCredentials;
import kasanari.authentication.spi.AuthPrincipal;
import kasanari.authentication.spi.AuthProvider;
import kasanari.authentication.spi.AuthProviderContext;
import kasanari.authentication.spi.AuthProviderMetadata;
import kasanari.authentication.spi.AuthRequest;
import kasanari.authentication.spi.CredentialScheme;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.realm.ldap.DirContextFactory;
import org.wildfly.security.auth.realm.ldap.LdapSecurityRealmBuilder;
import org.wildfly.security.auth.realm.ldap.SimpleDirContextFactoryBuilder;
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
        securityRealm = create(
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
                // cleanup password
                java.util.Arrays.fill(password, '\0');
            }

            return Optional.of(new AuthPrincipal(credentials.username()));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private SecurityRealm create(
            String url,
            String bindPrincipal,
            String bindPassword,
            String searchBaseDn,
            String rdnIdentifier) {
        DirContextFactory dirContextFactory = SimpleDirContextFactoryBuilder.builder()
                .setProviderUrl(url)
                .setSecurityAuthentication("simple")
                .setSecurityPrincipal(bindPrincipal)
                .setSecurityCredential(bindPassword)
                .build();

        return LdapSecurityRealmBuilder.builder()
                .setDirContextSupplier(() -> dirContextFactory.obtainDirContext(DirContextFactory.ReferralMode.IGNORE))
                .identityMapping()
                .searchRecursive()
                .setRdnIdentifier(rdnIdentifier)
                .setSearchDn(searchBaseDn)
                .build()
                .addDirectEvidenceVerification(false)
                .build();
    }
}
