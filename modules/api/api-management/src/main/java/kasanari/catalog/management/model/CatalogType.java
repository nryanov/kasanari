package kasanari.catalog.management.model;

public enum CatalogType {
    ICEBERG("ICEBERG"),
    PAIMON("PAIMON"),
    LANCE("LANCE");

    private final String value;

    CatalogType(String value) {
        this.value = value;
    }

    public static CatalogType fromValue(String value) {
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
