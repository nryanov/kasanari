package kasanari.auth.spi;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class AuthCredentials {
    private AuthCredentials() {
    }

    public static Optional<BasicCredentials> parseBasic(AuthRequest request) {
        return request.header("Authorization")
                .filter(value -> value.regionMatches(true, 0, "Basic ", 0, 6))
                .map(value -> value.substring(6).trim())
                .filter(value -> !value.isBlank())
                .flatMap(AuthCredentials::decodeBasic);
    }

    public static Optional<String> parseBearer(AuthRequest request) {
        return request.header("Authorization")
                .filter(value -> value.regionMatches(true, 0, "Bearer ", 0, 7))
                .map(value -> value.substring(7).trim())
                .filter(value -> !value.isBlank());
    }

    private static Optional<BasicCredentials> decodeBasic(String encoded) {
        try {
            var decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            var separator = decoded.indexOf(':');
            if (separator <= 0 || separator == decoded.length() - 1) {
                return Optional.empty();
            }
            return Optional.of(new BasicCredentials(
                    decoded.substring(0, separator),
                    decoded.substring(separator + 1)
            ));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record BasicCredentials(String username, String password) {
    }
}
