package kasanari.authorization.spi;

public record RoleBinding(
        String subject,
        String resource,
        String role,
        String effect
) {}
