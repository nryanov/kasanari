package kasanari.repository.management.security.model;

public record StoredRoleBinding(
        String subject,
        String resource,
        String role
) {
}
