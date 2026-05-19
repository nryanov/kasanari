package kasanari.auth.spi;

import java.util.Map;
import java.util.Optional;

public record AuthRequest(
        String method,
        String path,
        Map<String, String> headers,
        String remoteAddress
) {
    public Optional<String> header(String name) {
        if (headers == null || name == null) {
            return Optional.empty();
        }

        // todo: optimize
        for (var entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                var value = entry.getValue();
                return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
