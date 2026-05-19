package kasanari.auth.ldap;

import org.wildfly.security.auth.realm.ldap.DirContextFactory;
import org.wildfly.security.auth.realm.ldap.LdapSecurityRealmBuilder;
import org.wildfly.security.auth.realm.ldap.SimpleDirContextFactoryBuilder;
import org.wildfly.security.auth.server.SecurityRealm;

final class LdapSecurityRealms {
    private LdapSecurityRealms() {
    }

    static SecurityRealm create(
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
