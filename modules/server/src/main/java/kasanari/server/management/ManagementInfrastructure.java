package kasanari.server.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.catalog.management.model.CatalogType;
import kasanari.server.configuration.ManagementMetadataConfiguration;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.jdbi.v3.core.Handle;

import java.util.List;

@ApplicationScoped
public class ManagementInfrastructure {
    private final KasanariDataSource dataSource;
    private final JdbcTransactionManager txManager;
    private final CatalogMetadataRepository catalogRepository;
    private final RoleBindingRepository roleBindingRepository;
    private final Enforcer enforcer;

    public ManagementInfrastructure(ManagementMetadataConfiguration configuration, ObjectMapper objectMapper) {
        this.dataSource = new KasanariDataSource(configuration.jdbcProperties());
        this.txManager = new JdbcTransactionManager(dataSource);
        this.catalogRepository = new CatalogMetadataRepository(objectMapper);
        this.roleBindingRepository = new RoleBindingRepository();
        this.enforcer = createEnforcer();
        initSchema();
        initRolePermissions();
        reloadGroupingPolicies();
    }

    public JdbcTransactionManager txManager() {
        return txManager;
    }

    public CatalogMetadataRepository catalogRepository() {
        return catalogRepository;
    }

    public RoleBindingRepository roleBindingRepository() {
        return roleBindingRepository;
    }

    public Enforcer enforcer() {
        return enforcer;
    }

    public void reloadGroupingPolicies() {
        var groupingPolicies = enforcer.getGroupingPolicy();
        for (var policy : groupingPolicies) {
            enforcer.removeGroupingPolicy(policy);
        }

        var storedBindings = txManager.inTransactionR(tx -> roleBindingRepository.list(tx, null, null));
        for (var binding : storedBindings) {
            enforcer.addGroupingPolicy(binding.subject(), binding.role().toString(), binding.catalogType().toString());
        }
    }

    private void initSchema() {
        txManager.inTransaction(tx -> {
            tx.createUpdate(ManagementJdbcQueries.CREATE_CATALOG_REGISTRY_DDL).execute();
            tx.createUpdate(ManagementJdbcQueries.CREATE_CATALOG_SECRETS_DDL).execute();
            tx.createUpdate(ManagementJdbcQueries.CREATE_ROLE_BINDINGS_DDL).execute();
        });
    }

    private Enforcer createEnforcer() {
        var modelText = """
                [request_definition]
                r = sub, dom, obj, act

                [policy_definition]
                p = sub, dom, obj, act

                [role_definition]
                g = _, _, _

                [policy_effect]
                e = some(where (p.eft == allow))

                [matchers]
                m = (r.sub == "root") || (g(r.sub, p.sub, r.dom) && r.dom == p.dom && r.obj == p.obj && r.act == p.act)
                """;

        var model = new Model();
        model.loadModelFromText(modelText);
        return new Enforcer(model);
    }

    private void initRolePermissions() {
        for (var domain : allDomains()) {
            enforcer.addPolicy("catalog_admin", domain, "catalog", "create");
            enforcer.addPolicy("catalog_admin", domain, "catalog", "update");
            enforcer.addPolicy("catalog_admin", domain, "catalog", "delete");
            enforcer.addPolicy("catalog_admin", domain, "catalog", "get");

            enforcer.addPolicy("catalog_reader", domain, "catalog", "get");

            enforcer.addPolicy("security_admin", domain, "security.roles", "get");
            enforcer.addPolicy("security_admin", domain, "security.roles", "update");
            enforcer.addPolicy("security_admin", domain, "security.roles", "delete");

            enforcer.addPolicy("security_reader", domain, "security.roles", "get");
        }
    }

    private List<String> allDomains() {
        return List.of(
                CatalogType.ICEBERG.toString(),
                CatalogType.PAIMON.toString(),
                CatalogType.LANCE.toString()
        );
    }

    @PreDestroy
    void close() {
        dataSource.close();
    }
}
