package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;
import kasanari.repository.management.security.model.StoredRoleBinding;
import kasanari.repository.management.security.postgres.JdbcManagementSecurityQueries;
import kasanari.repository.management.security.postgres.JdbcRoleBindingRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasbinPolicyReloaderTest {
    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();

    private static PostgresHelper postgresHelper;
    private static JdbcTransactionManager txManager;
    private static JdbcRoleBindingRepository repository;
    private static CasbinPolicyHolder policyHolder;
    private static CasbinPolicyReloader reloader;

    @BeforeAll
    static void setup() {
        POSTGRES.start();
        postgresHelper = new PostgresHelper(POSTGRES);
        var dataSource = new KasanariDataSource(Map.of(
                KasanariDataSourceConfiguration.URI, POSTGRES.jdbcUrl(),
                KasanariDataSourceConfiguration.USER, POSTGRES.username(),
                KasanariDataSourceConfiguration.PASSWORD, POSTGRES.password()
        ));
        txManager = new JdbcTransactionManager(dataSource);
        repository = new JdbcRoleBindingRepository();
        txManager.inTransaction(tx -> {
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDINGS_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDING_REVISION_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.INSERT_ROLE_BINDING_REVISION).execute();
        });

        policyHolder = new CasbinPolicyHolder();
        reloader = new CasbinPolicyReloader(txManager, repository, policyHolder);
    }

    @AfterAll
    static void cleanup() {
        POSTGRES.stop();
    }

    @BeforeEach
    void beforeEach() {
        postgresHelper.truncateTable("kasanari_role_bindings");
        txManager.inTransaction(tx -> tx.createUpdate(JdbcManagementSecurityQueries.RESET_ROLE_BINDING_REVISION).execute());
        reloader.reloadIfChanged();
    }

    @Test
    void reloadIfChangedSkipsWhenRevisionUnchanged() {
        reloader.reloadIfChanged();
        var first = policyHolder.current();

        reloader.reloadIfChanged();

        assertSame(first, policyHolder.current());
    }

    @Test
    void reloadIfChangedSwapsEnforcerWhenRevisionChanges() {
        reloader.reloadIfChanged();
        var before = policyHolder.current();

        txManager.inTransaction(tx -> {
            repository.add(tx, List.of(
                    new StoredRoleBinding("alice", "iceberg/prod", CasbinRoles.ICEBERG_CATALOG_VIEWER)
            ));
            repository.bumpRevision(tx);
        });
        reloader.reloadIfChanged();

        assertFalse(before.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(policyHolder.current().enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
    }
}
