package kasanari.instrumentation.runtime;

import kasanari.instrumentation.spi.CatalogRequestListener;
import kasanari.instrumentation.spi.CatalogRequestScope;
import kasanari.instrumentation.spi.paimon.PaimonCatalogRequestContext;
import org.slf4j.MDC;

import java.util.List;
import java.util.function.Supplier;

public final class PaimonCatalogRequestPipeline {
    private final List<CatalogRequestListener> listeners;

    public PaimonCatalogRequestPipeline(List<CatalogRequestListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    public <T> T execute(PaimonCatalogRequestContext ctx, Supplier<T> action) {
        var scope = new CatalogRequestScope();
        scope.markStarted();
        putMdc(ctx.catalogName(), ctx.operation().name(), ctx.subject());
        try {
            listeners.forEach(listener -> listener.paimonBefore(ctx, scope));
            try {
                T result = action.get();
                listeners.forEach(listener -> listener.paimonAfter(ctx, scope, result));
                return result;
            } catch (RuntimeException | Error e) {
                listeners.forEach(listener -> listener.paimonOnError(ctx, scope, e));
                throw e;
            }
        } finally {
            clearMdc();
        }
    }

    public void notifyDenied(PaimonCatalogRequestContext ctx) {
        listeners.forEach(listener -> listener.paimonOnDenied(ctx));
    }

    private static void putMdc(String catalog, String operation, String subject) {
        MDC.put("catalog", catalog);
        MDC.put("operation", operation);
        MDC.put("subject", subject);
    }

    private static void clearMdc() {
        MDC.remove("catalog");
        MDC.remove("operation");
        MDC.remove("subject");
    }
}
