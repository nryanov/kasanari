package kasanari.instrumentation.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kasanari.instrumentation.spi.CatalogRequestListener;
import kasanari.instrumentation.spi.CatalogRequestListenerContext;
import kasanari.instrumentation.spi.CatalogRequestScope;
import kasanari.instrumentation.spi.iceberg.IcebergCatalogRequestContext;
import kasanari.instrumentation.spi.lance.LanceCatalogRequestContext;
import kasanari.instrumentation.spi.paimon.PaimonCatalogRequestContext;

import java.time.Duration;

@ApplicationScoped
public class MetricsCatalogRequestListener implements CatalogRequestListener {
    private static final String LISTENER_TYPE = "metrics";

    private static final String COUNTER_NAME = "kasanari.catalog.request.total";
    private static final String TIMER_NAME = "kasanari.catalog.request.duration";

    private static final String TAG_CATALOG_TYPE = "catalog";
    private static final String TAG_OPERATION = "operation";
    private static final String TAG_CATALOG_NAME = "catalogName";
    private static final String TAG_SUBJECT = "subject";
    private static final String TAG_OUTCOME = "outcome";

    private static final String CATALOG_ICEBERG = "iceberg";
    private static final String CATALOG_PAIMON = "paimon";
    private static final String CATALOG_LANCE = "lance";

    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_ERROR = "error";
    private static final String OUTCOME_DENIED = "denied";

    private static final String UNKNOWN_SUBJECT = "unknown";

    private final MeterRegistry registry;

    @Inject
    public MetricsCatalogRequestListener(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String type() {
        return LISTENER_TYPE;
    }

    @Override
    public void initialize(CatalogRequestListenerContext context) {}

    @Override
    public void icebergAfter(IcebergCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
        record(CATALOG_ICEBERG, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_SUCCESS, scope.elapsed());
    }

    @Override
    public void icebergOnError(IcebergCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
        record(CATALOG_ICEBERG, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_ERROR, scope.elapsed());
    }

    @Override
    public void icebergOnDenied(IcebergCatalogRequestContext ctx) {
        record(CATALOG_ICEBERG, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_DENIED, null);
    }

    @Override
    public void paimonAfter(PaimonCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
        record(CATALOG_PAIMON, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_SUCCESS, scope.elapsed());
    }

    @Override
    public void paimonOnError(PaimonCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
        record(CATALOG_PAIMON, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_ERROR, scope.elapsed());
    }

    @Override
    public void paimonOnDenied(PaimonCatalogRequestContext ctx) {
        record(CATALOG_PAIMON, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_DENIED, null);
    }

    @Override
    public void lanceAfter(LanceCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
        record(CATALOG_LANCE, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_SUCCESS, scope.elapsed());
    }

    @Override
    public void lanceOnError(LanceCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
        record(CATALOG_LANCE, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_ERROR, scope.elapsed());
    }

    @Override
    public void lanceOnDenied(LanceCatalogRequestContext ctx) {
        record(CATALOG_LANCE, ctx.catalogName(), ctx.operation().name(), ctx.subject(), OUTCOME_DENIED, null);
    }

    private void record(
            String catalog,
            String catalogName,
            String operation,
            String subject,
            String outcome,
            Duration duration
    ) {
        var tags = Tags.of(
                TAG_CATALOG_TYPE, catalog,
                TAG_OPERATION, operation,
                TAG_CATALOG_NAME, catalogName,
                TAG_SUBJECT, subject == null || subject.isBlank() ? UNKNOWN_SUBJECT : subject,
                TAG_OUTCOME, outcome
        );

        registry.counter(COUNTER_NAME, tags).increment();

        if (duration != null) {
            registry.timer(TIMER_NAME, tags).record(duration);
        }
    }
}
