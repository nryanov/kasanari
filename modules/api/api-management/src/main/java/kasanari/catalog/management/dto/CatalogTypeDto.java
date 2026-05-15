package kasanari.catalog.management.dto;

public enum CatalogTypeDto {
    ICEBERG("ICEBERG"),
    PAIMON("PAIMON"),
    LANCE("LANCE");

    private final String value;

    CatalogTypeDto(String value) {
        this.value = value;
    }

    public static CatalogTypeDto fromValue(String value) {
        for (var item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown catalog type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
