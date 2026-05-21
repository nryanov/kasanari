package kasanari.instrumentation.audit;

import kasanari.instrumentation.spi.CatalogRequestListener;
import kasanari.instrumentation.spi.CatalogRequestListenerContext;
import kasanari.instrumentation.spi.CatalogRequestScope;
import kasanari.instrumentation.spi.iceberg.IcebergCatalogRequestContext;
import kasanari.instrumentation.spi.lance.LanceCatalogRequestContext;
import kasanari.instrumentation.spi.paimon.PaimonCatalogRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditCatalogRequestListener implements CatalogRequestListener {
    private static final Logger logger = LoggerFactory.getLogger(AuditCatalogRequestListener.class);

    @Override
    public String type() {
        return "audit";
    }

    @Override
    public void initialize(CatalogRequestListenerContext context) {
    }

    @Override
    public void icebergBefore(IcebergCatalogRequestContext ctx, CatalogRequestScope scope) {
        audit(ctx.subject(), ctx.operation().name(), ctx.catalogName());
    }

    @Override
    public void icebergOnDenied(IcebergCatalogRequestContext ctx) {
        auditDenied(ctx.subject(), ctx.operation().name(), ctx.catalogName());
    }

    @Override
    public void paimonBefore(PaimonCatalogRequestContext ctx, CatalogRequestScope scope) {
        audit(ctx.subject(), ctx.operation().name(), ctx.catalogName());
    }

    @Override
    public void paimonOnDenied(PaimonCatalogRequestContext ctx) {
        auditDenied(ctx.subject(), ctx.operation().name(), ctx.catalogName());
    }

    @Override
    public void lanceBefore(LanceCatalogRequestContext ctx, CatalogRequestScope scope) {
        audit(ctx.subject(), ctx.operation().name(), ctx.catalogName());
    }

    @Override
    public void lanceOnDenied(LanceCatalogRequestContext ctx) {
        auditDenied(ctx.subject(), ctx.operation().name(), ctx.catalogName());
    }

    private static void audit(String subject, String operation, String catalogName) {
        logger.info("Audit: subject='{}' operation={} catalog='{}'", subject, operation, catalogName);
    }

    private static void auditDenied(String subject, String operation, String catalogName) {
        logger.warn("Audit denied: subject='{}' operation={} catalog='{}'", subject, operation, catalogName);
    }
}
