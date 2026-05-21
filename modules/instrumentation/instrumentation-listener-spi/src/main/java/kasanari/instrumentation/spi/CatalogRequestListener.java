package kasanari.instrumentation.spi;

import kasanari.instrumentation.spi.iceberg.IcebergCatalogRequestContext;
import kasanari.instrumentation.spi.lance.LanceCatalogRequestContext;
import kasanari.instrumentation.spi.paimon.PaimonCatalogRequestContext;

public interface CatalogRequestListener {
    String type();

    void initialize(CatalogRequestListenerContext context);

    default void icebergBefore(IcebergCatalogRequestContext ctx, CatalogRequestScope scope) {
    }

    default void icebergAfter(IcebergCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
    }

    default void icebergOnError(IcebergCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
    }

    default void icebergOnDenied(IcebergCatalogRequestContext ctx) {
    }

    default void paimonBefore(PaimonCatalogRequestContext ctx, CatalogRequestScope scope) {
    }

    default void paimonAfter(PaimonCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
    }

    default void paimonOnError(PaimonCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
    }

    default void paimonOnDenied(PaimonCatalogRequestContext ctx) {
    }

    default void lanceBefore(LanceCatalogRequestContext ctx, CatalogRequestScope scope) {
    }

    default void lanceAfter(LanceCatalogRequestContext ctx, CatalogRequestScope scope, Object result) {
    }

    default void lanceOnError(LanceCatalogRequestContext ctx, CatalogRequestScope scope, Throwable error) {
    }

    default void lanceOnDenied(LanceCatalogRequestContext ctx) {
    }
}
