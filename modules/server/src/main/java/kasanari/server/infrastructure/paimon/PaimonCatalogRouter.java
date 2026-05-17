package kasanari.server.infrastructure.paimon;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.paimon.KasanariPaimonCatalogFactory;
import kasanari.catalog.paimon.PaimonCatalogAdapter;
import kasanari.catalog.paimon.ProxyPaimonCatalogFactory;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.repository.management.common.model.CatalogType;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PaimonCatalogRouter {
    private static final Logger logger = Logger.getLogger(PaimonCatalogRouter.class);

    // todo: regularly fetch actual catalogs from DB
    private final ManagementCatalogService catalogService;
    private final Map<String, PaimonCatalogAdapter> paimonCatalogs;

    private final KasanariPaimonCatalogFactory kasanariPaimonCatalogFactory;
    private final ProxyPaimonCatalogFactory proxyPaimonCatalogFactory;

    public PaimonCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.paimonCatalogs = new ConcurrentHashMap<>();

        this.kasanariPaimonCatalogFactory = new KasanariPaimonCatalogFactory();
        this.proxyPaimonCatalogFactory = new ProxyPaimonCatalogFactory();
    }

    @PostConstruct
    public void initializeCatalogs() {
        var catalogs = catalogService.list(CatalogType.PAIMON);

        for (var catalog : catalogs) {
            switch (catalog.catalogMode()) {
                case INTERNAL -> {
                    var instance = kasanariPaimonCatalogFactory.create(
                            catalog.spec().fileIoProperties(),
                            catalog.spec().catalogProperties()
                    );

                    paimonCatalogs.put(catalog.catalogName(), instance);
                }
                case PROXY -> {
                    var instance = proxyPaimonCatalogFactory.create(
                            catalog.spec().fileIoProperties(),
                            catalog.spec().catalogProperties()
                    );

                    paimonCatalogs.put(catalog.catalogName(), instance);
                }
            }
        }
    }

    public PaimonCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = paimonCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Paimon catalog wasn't found");
        }

        return maybeCatalog;
    }
}
