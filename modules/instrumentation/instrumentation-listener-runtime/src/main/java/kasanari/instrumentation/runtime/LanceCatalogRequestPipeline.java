package kasanari.instrumentation.runtime;

import kasanari.instrumentation.spi.CatalogRequestListener;
import kasanari.instrumentation.spi.CatalogRequestScope;
import kasanari.instrumentation.spi.lance.LanceCatalogRequestContext;
import org.slf4j.MDC;

import java.util.List;
import java.util.function.Supplier;

public final class LanceCatalogRequestPipeline {
    private final List<CatalogRequestListener> listeners;

    public LanceCatalogRequestPipeline(List<CatalogRequestListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    public <T> T execute(LanceCatalogRequestContext ctx, Supplier<T> action) {
        var scope = new CatalogRequestScope();
        scope.markStarted();
        putMdc(ctx.catalogName(), ctx.operation().name(), ctx.subject());
        try {
            listeners.forEach(listener -> listener.lanceBefore(ctx, scope));
            try {
                T result = action.get();
                listeners.forEach(listener -> listener.lanceAfter(ctx, scope, result));
                return result;
            } catch (RuntimeException | Error e) {
                listeners.forEach(listener -> listener.lanceOnError(ctx, scope, e));
                throw e;
            }
        } finally {
            clearMdc();
        }
    }

    public void notifyDenied(LanceCatalogRequestContext ctx) {
        listeners.forEach(listener -> listener.lanceOnDenied(ctx));
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
