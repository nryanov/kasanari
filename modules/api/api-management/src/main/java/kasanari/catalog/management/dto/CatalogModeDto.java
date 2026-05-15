package kasanari.catalog.management.dto;

public enum CatalogModeDto {
    INTERNAL("INTERNAL"),
    PROXY("PROXY");

    private final String value;

    CatalogModeDto(String value) {
        this.value = value;
    }

    public static CatalogModeDto fromValue(String value) {
        for (var item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown catalog mode: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
