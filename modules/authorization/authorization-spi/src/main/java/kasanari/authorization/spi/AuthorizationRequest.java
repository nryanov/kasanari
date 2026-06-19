package kasanari.authorization.spi;

public record AuthorizationRequest(
        String subject,
        String resource,
        Permission permission
) {
    public AuthorizationRequest(String subject, String resource, String permissionName) {
        this(subject, resource, Permission.fromName(permissionName));
    }

    public AuthorizationRequest {
        AuthorizationResource.parse(resource);
    }
}
