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

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class CasbinAuthorizationProvider implements AuthorizationProvider {
    private final Set<String> superuserSubjects = new HashSet<>();
    private CasbinPolicyEngine policyEngine;
    private CasbinRoleBindingAdministration roleBindingAdministration;
    private ScheduledExecutorService refreshExecutor;

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
        initSchema(txManager);

        var policyBootstrap = new CasbinPolicyBootstrap();
        policyEngine = new CasbinPolicyEngine(txManager, roleBindingRepository, policyBootstrap);
        roleBindingAdministration = new CasbinRoleBindingAdministration(
                txManager,
                roleBindingRepository,
                policyEngine
        );

        policyEngine.reloadIfChanged();
        startRefreshScheduler(context, dataSource);
    }

    @Override
    public boolean isAuthorized(AuthorizationRequest request) {
        if (superuserSubjects.contains(request.subject().toLowerCase(Locale.ROOT))) {
            return true;
        }

        Enforcer enforcer = policyEngine.current();
        if (enforcer == null) {
            return false;
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

    private void startRefreshScheduler(AuthorizationProviderContext context, KasanariDataSource dataSource) {
        var refreshInterval = parseRefreshInterval(context.getOptional("refresh-interval").orElse(null));

        refreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "casbin-policy-refresh");
            thread.setDaemon(true);
            return thread;
        });

        refreshExecutor.scheduleWithFixedDelay(
                policyEngine::reloadIfChanged,
                refreshInterval.toSeconds(),
                refreshInterval.toSeconds(),
                TimeUnit.SECONDS
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(dataSource), "casbin-policy-shutdown"));
    }

    private void shutdown(KasanariDataSource dataSource) {
        if (refreshExecutor != null) {
            refreshExecutor.shutdown();
        }
        dataSource.close();
    }

    private void initSchema(kasanari.repository.core.TransactionManager<org.jdbi.v3.core.Handle> txManager) {
        txManager.inTransaction(tx -> {
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDINGS_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDING_REVISION_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.INSERT_ROLE_BINDING_REVISION).execute();
        });
    }

    private Map<String, String> resolveJdbcProperties(AuthorizationProviderContext context) {
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

    private Duration parseRefreshInterval(String value) {
        var defaultInterval = Duration.ofMinutes(5);
        if (value == null || value.isBlank()) {
            return defaultInterval;
        }

        var trimmed = value.trim().toLowerCase(Locale.ROOT);
        if (trimmed.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(trimmed.substring(0, trimmed.length() - 2)));
        }
        if (trimmed.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        if (trimmed.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        if (trimmed.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        if (trimmed.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }

        return Duration.parse(trimmed);
    }
}
