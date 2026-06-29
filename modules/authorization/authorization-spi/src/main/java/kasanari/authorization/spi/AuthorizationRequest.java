package kasanari.authorization.spi;

public record AuthorizationRequest(
        String subject,
        String resource,
        Permission permission
) {}
