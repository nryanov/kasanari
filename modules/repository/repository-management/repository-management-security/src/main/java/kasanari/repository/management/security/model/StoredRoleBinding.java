package kasanari.repository.management.security.model;

public record StoredRoleBinding(
        String subject,
        String role,
        String resource
) {
}
