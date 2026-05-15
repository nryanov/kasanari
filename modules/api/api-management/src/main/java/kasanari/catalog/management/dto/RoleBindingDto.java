package kasanari.catalog.management.dto;

public class RoleBindingDto {
    public enum RoleEnum {
        CATALOG_ADMIN("catalog_admin"),
        CATALOG_READER("catalog_reader"),
        SECURITY_ADMIN("security_admin"),
        SECURITY_READER("security_reader");

        private final String value;

        RoleEnum(String value) {
            this.value = value;
        }

        public static RoleEnum fromValue(String value) {
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

    private String subject;
    private CatalogTypeDto catalogType;
    private RoleEnum role;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public CatalogTypeDto getCatalogType() {
        return catalogType;
    }

    public void setCatalogType(CatalogTypeDto catalogType) {
        this.catalogType = catalogType;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }
}
