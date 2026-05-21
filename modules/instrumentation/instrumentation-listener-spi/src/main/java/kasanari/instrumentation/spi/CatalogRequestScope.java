package kasanari.instrumentation.spi;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class CatalogRequestScope {
    private long startedAtNanos;
    private final Map<String, Object> data = new HashMap<>();

    public void markStarted() {
        startedAtNanos = System.nanoTime();
    }

    public Duration elapsed() {
        if (startedAtNanos == 0L) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(System.nanoTime() - startedAtNanos);
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public <T> T get(String key) {
        @SuppressWarnings("unchecked")
        T value = (T) data.get(key);
        return value;
    }
}
