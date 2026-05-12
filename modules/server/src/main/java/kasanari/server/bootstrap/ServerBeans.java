package kasanari.server.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.management.security.ManagementSecurityService;

import javax.sql.DataSource;

@ApplicationScoped
public class ServerBeans {
    @Singleton
    @Produces
    @Named("management-datasource")
    public DataSource dataSource() {
        return null;
    }

    @Singleton
    @Produces
    public ManagementCatalogService managementCatalogService() {
        return null;
    }

    @Singleton
    @Produces
    public ManagementSecurityService managementSecurityService() {
        return null;
    }
}
