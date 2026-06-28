package kasanari.core.model;

import java.util.Locale;

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

    public String resourceEngine() {
        return value.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
