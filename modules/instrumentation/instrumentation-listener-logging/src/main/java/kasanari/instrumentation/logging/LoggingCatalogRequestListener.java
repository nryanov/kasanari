package kasanari.instrumentation.logging;

import kasanari.instrumentation.spi.CatalogRequestListener;
import kasanari.instrumentation.spi.CatalogRequestListenerContext;
import kasanari.instrumentation.spi.CatalogRequestScope;
import kasanari.instrumentation.spi.iceberg.IcebergCatalogRequestContext;
import kasanari.instrumentation.spi.lance.LanceCatalogRequestContext;
import kasanari.instrumentation.spi.paimon.PaimonCatalogRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingCatalogRequestListener implements CatalogRequestListener {
    private static final Logger icebergLogger = LoggerFactory.getLogger("IcebergCatalogAdapter");
    private static final Logger paimonLogger = LoggerFactory.getLogger("PaimonCatalogAdapter");
    private static final Logger lanceLogger = LoggerFactory.getLogger("LanceCatalogAdapter");

    @Override
    public String type() {
        return "logging";
    }

    @Override
    public void initialize(CatalogRequestListenerContext context) {
    }

    @Override
    public void icebergBefore(IcebergCatalogRequestContext ctx, CatalogRequestScope scope) {
        icebergLogger.info("Attempt to {} on catalog `{}` ({})", ctx.operation(), ctx.catalogName(), ctx.attributes());
    }

    @Override
    public void icebergAfter(IcebergCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
        icebergLogger.info(
                "Successfully completed {} on catalog `{}` in {} ms",
                ctx.operation(),
                ctx.catalogName(),
                scope.elapsed().toMillis()
        );
    }

    @Override
    public void icebergOnError(IcebergCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
        icebergLogger.error(
                "Error during {} on catalog `{}`: {}",
                ctx.operation(),
                ctx.catalogName(),
                error.getMessage()
        );
    }

    @Override
    public void icebergOnDenied(IcebergCatalogRequestContext ctx) {
        icebergLogger.info("Denied {} on catalog `{}` for subject `{}`", ctx.operation(), ctx.catalogName(), ctx.subject());
    }

    @Override
    public void paimonBefore(PaimonCatalogRequestContext ctx, CatalogRequestScope scope) {
        paimonLogger.info("Attempt to {} on catalog `{}` ({})", ctx.operation(), ctx.catalogName(), ctx.attributes());
    }

    @Override
    public void paimonAfter(PaimonCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
        paimonLogger.info(
                "Successfully completed {} on catalog `{}` in {} ms",
                ctx.operation(),
                ctx.catalogName(),
                scope.elapsed().toMillis()
        );
    }

    @Override
    public void paimonOnError(PaimonCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
        paimonLogger.error(
                "Error during {} on catalog `{}`: {}",
                ctx.operation(),
                ctx.catalogName(),
                error.getMessage()
        );
    }

    @Override
    public void paimonOnDenied(PaimonCatalogRequestContext ctx) {
        paimonLogger.info("Denied {} on catalog `{}` for subject `{}`", ctx.operation(), ctx.catalogName(), ctx.subject());
    }

    @Override
    public void lanceBefore(LanceCatalogRequestContext ctx, CatalogRequestScope scope) {
        lanceLogger.info("Attempt to {} on catalog `{}` ({})", ctx.operation(), ctx.catalogName(), ctx.attributes());
    }

    @Override
    public void lanceAfter(LanceCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
        lanceLogger.info(
                "Successfully completed {} on catalog `{}` in {} ms",
                ctx.operation(),
                ctx.catalogName(),
                scope.elapsed().toMillis()
        );
    }

    @Override
    public void lanceOnError(LanceCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
        lanceLogger.error(
                "Error during {} on catalog `{}`: {}",
                ctx.operation(),
                ctx.catalogName(),
                error.getMessage()
        );
    }

    @Override
    public void lanceOnDenied(LanceCatalogRequestContext ctx) {
        lanceLogger.info("Denied {} on catalog `{}` for subject `{}`", ctx.operation(), ctx.catalogName(), ctx.subject());
    }
}
