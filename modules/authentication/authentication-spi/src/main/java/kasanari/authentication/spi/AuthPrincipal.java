package kasanari.authentication.spi;

import java.util.Map;
import java.util.Set;

public record AuthPrincipal(
        String name,
        Set<String> roles,
        Map<String, Object> attributes
) {
    public AuthPrincipal(String name) {
        this(name, Set.of(), Map.of());
    }
}
