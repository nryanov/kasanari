package kasanari.instrumentation.runtime;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import kasanari.instrumentation.spi.CatalogRequestListener;

import java.util.ArrayList;

@Startup
@ApplicationScoped
public class CatalogRequestInstrumentation {
    private final IcebergCatalogRequestPipeline icebergPipeline;
    private final PaimonCatalogRequestPipeline paimonPipeline;
    private final LanceCatalogRequestPipeline lancePipeline;

    @Inject
    public CatalogRequestInstrumentation(
            InstrumentationConfiguration configuration,
            Instance<CatalogRequestListener> cdiListeners
    ) {
        var enabledTypes = CatalogRequestListenerRegistry.parseEnabledTypes(configuration.listeners());
        var configProperties = CatalogRequestListenerRegistry.readConfigProperties();
        var listeners = new ArrayList<>(CatalogRequestListenerRegistry.load(configuration));
        cdiListeners.forEach(listener ->
                CatalogRequestListenerRegistry.registerListener(listener, enabledTypes, configProperties, listeners)
        );
        this.icebergPipeline = new IcebergCatalogRequestPipeline(listeners);
        this.paimonPipeline = new PaimonCatalogRequestPipeline(listeners);
        this.lancePipeline = new LanceCatalogRequestPipeline(listeners);
    }

    public IcebergCatalogRequestPipeline icebergPipeline() {
        return icebergPipeline;
    }

    public PaimonCatalogRequestPipeline paimonPipeline() {
        return paimonPipeline;
    }

    public LanceCatalogRequestPipeline lancePipeline() {
        return lancePipeline;
    }
}
