package kasanari.repository.management.catalog.model;

public enum CatalogMode {
    INTERNAL("INTERNAL"),
    PROXY("PROXY");

    private final String value;

    CatalogMode(String value) {
        this.value = value;
    }

    public static CatalogMode fromValue(String value) {
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
