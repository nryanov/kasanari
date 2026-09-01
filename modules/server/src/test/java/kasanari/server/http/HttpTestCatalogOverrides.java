package kasanari.server.http;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.server.infrastructure.iceberg.IcebergCatalogRouter;
import kasanari.server.infrastructure.lance.LanceCatalogRouter;
import kasanari.server.infrastructure.paimon.PaimonCatalogRouter;

import static org.mockito.Mockito.mock;

/**
 * Replaces catalog routers and {@link ManagementCatalogService} before Quarkus startup so HTTP
 * tests never open the management JDBC pool.
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class HttpTestCatalogOverrides {
    @Produces
    @Singleton
    ManagementCatalogService managementCatalogService() {
        return mock(ManagementCatalogService.class);
    }

    @Produces
    @Singleton
    IcebergCatalogRouter icebergCatalogRouter() {
        return mock(IcebergCatalogRouter.class);
    }

    @Produces
    @Singleton
    PaimonCatalogRouter paimonCatalogRouter() {
        return mock(PaimonCatalogRouter.class);
    }

    @Produces
    @Singleton
    LanceCatalogRouter lanceCatalogRouter() {
        return mock(LanceCatalogRouter.class);
    }
}
