package kasanari.catalog.management.dto;

public class RoleBindingDto {
    private String subject;
    private CatalogTypeDto catalogType;
    private String role;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
