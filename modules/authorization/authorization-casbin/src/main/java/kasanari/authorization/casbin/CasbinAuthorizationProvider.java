package kasanari.authorization.casbin;

import kasanari.authorization.spi.AuthorizationProvider;
import kasanari.authorization.spi.AuthorizationProviderContext;
import kasanari.authorization.spi.AuthorizationRequest;
import kasanari.authorization.spi.RoleBindingAdministration;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.management.security.postgres.JdbcManagementSecurityQueries;
import kasanari.repository.management.security.postgres.JdbcRoleBindingRepository;
import org.casbin.jcasbin.main.Enforcer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CasbinAuthorizationProvider implements AuthorizationProvider {
    private final Set<String> superuserSubjects = new HashSet<>();
    private Enforcer enforcer;
    private CasbinRoleBindingAdministration roleBindingAdministration;

    @Override
    public String type() {
        return "casbin";
    }

    @Override
    public void initialize(AuthorizationProviderContext context) {
        context.getOptional("superuser-subjects")
                .ifPresent(value -> Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(subject -> !subject.isEmpty())
                        .map(subject -> subject.toLowerCase(Locale.ROOT))
                        .forEach(superuserSubjects::add));
        if (superuserSubjects.isEmpty()) {
            superuserSubjects.add("root");
        }

        var dataSource = new KasanariDataSource(resolveJdbcProperties(context));
        var txManager = new JdbcTransactionManager(dataSource);
        var roleBindingRepository = new JdbcRoleBindingRepository();
        enforcer = CasbinEnforcerFactory.createEnforcer();
        initSchema(txManager);
        roleBindingAdministration = new CasbinRoleBindingAdministration(txManager, roleBindingRepository, enforcer);
        roleBindingAdministration.reloadPolicies();
    }

    @Override
    public boolean isAuthorized(AuthorizationRequest request) {
        if (superuserSubjects.contains(request.subject().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return enforcer.enforce(
                request.subject(),
                request.resource(),
                request.permission().wireName()
        );
    }

    @Override
    public Optional<RoleBindingAdministration> roleBindings() {
        return Optional.of(roleBindingAdministration);
    }

    private static void initSchema(kasanari.repository.core.TransactionManager<org.jdbi.v3.core.Handle> txManager) {
        txManager.inTransaction(tx -> tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDINGS_DDL).execute());
    }

    private static Map<String, String> resolveJdbcProperties(AuthorizationProviderContext context) {
        var uri = context.getOptional("jdbc.uri");
        var user = context.getOptional("jdbc.user");
        var password = context.getOptional("jdbc.password");

        if (uri.isEmpty() || user.isEmpty() || password.isEmpty()) {
            throw new IllegalStateException(
                    "Casbin authorization requires JDBC properties: jdbc.uri, jdbc.user, jdbc.password via kasanari.authorization.casbin.*");
        }

        var properties = new HashMap<String, String>();
        properties.put("uri", uri.get());
        properties.put("kasanari.jdbc.user", user.get());
        properties.put("kasanari.jdbc.password", password.get());

        return properties;
    }
}
