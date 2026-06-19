package kasanari.authorization.spi;

public record RoleBinding(
        String subject,
        String role,
        String resource
) {
    public RoleBinding {
        AuthorizationResource.parse(resource);
    }
}
