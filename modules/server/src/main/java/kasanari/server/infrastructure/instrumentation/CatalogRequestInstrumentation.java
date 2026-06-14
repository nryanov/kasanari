package kasanari.server.infrastructure.instrumentation;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kasanari.instrumentation.metrics.MetricsCatalogRequestListener;
import kasanari.instrumentation.runtime.CatalogRequestListenerRegistry;
import kasanari.instrumentation.runtime.IcebergCatalogRequestPipeline;
import kasanari.instrumentation.runtime.InstrumentationConfiguration;
import kasanari.instrumentation.runtime.LanceCatalogRequestPipeline;
import kasanari.instrumentation.runtime.PaimonCatalogRequestPipeline;

import java.util.List;
import java.util.List;

@Startup
@ApplicationScoped
public class CatalogRequestInstrumentation {
    private final IcebergCatalogRequestPipeline icebergPipeline;
    private final PaimonCatalogRequestPipeline paimonPipeline;
    private final LanceCatalogRequestPipeline lancePipeline;

    @Inject
    public CatalogRequestInstrumentation(
            InstrumentationConfiguration configuration,
            MetricsCatalogRequestListener metricsListener
    ) {
        var listeners = CatalogRequestListenerRegistry.assembleListeners(configuration, List.of(metricsListener));
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
