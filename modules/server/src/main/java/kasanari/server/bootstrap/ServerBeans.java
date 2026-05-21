package kasanari.server.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.server.configuration.ManagementMetadataConfiguration;

@ApplicationScoped
public class ServerBeans {
    private final ManagementMetadataConfiguration metadataConfiguration;

    @Inject
    public ServerBeans(ManagementMetadataConfiguration metadataConfiguration) {
        this.metadataConfiguration = metadataConfiguration;
    }

    @Singleton
    @Produces
    @Named("management-datasource")
    public KasanariDataSource managementDataSource() {
        return new KasanariDataSource(metadataConfiguration.jdbcProperties());
    }

    @Singleton
    @Produces
    public ManagementCatalogService managementCatalogService(
            @Named("management-datasource") KasanariDataSource dataSource,
            ObjectMapper objectMapper) {
        return new ManagementCatalogService(dataSource, objectMapper);
    }

}
