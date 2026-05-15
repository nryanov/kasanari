package kasanari.repository.management.security.model;

public enum Role {
    CATALOG_ADMIN("catalog_admin"),
    CATALOG_READER("catalog_reader"),
    SECURITY_ADMIN("security_admin"),
    SECURITY_READER("security_reader");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Role fromValue(String value) {
        for (var item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
