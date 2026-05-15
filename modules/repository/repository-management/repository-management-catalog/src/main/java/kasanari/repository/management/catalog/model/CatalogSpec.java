package kasanari.repository.management.catalog.model;

public class CatalogSpec {
    public enum Type {
        ICEBERG("ICEBERG"),
        PAIMON("PAIMON"),
        LANCE("LANCE");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type fromValue(String value) {
            for (var item : values()) {
                if (item.value.equals(value)) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Unknown spec type: " + value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private Type type;
    private CatalogSpecModeConfig modeConfig;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CatalogSpecModeConfig getModeConfig() {
        return modeConfig;
    }

    public void setModeConfig(CatalogSpecModeConfig modeConfig) {
        this.modeConfig = modeConfig;
    }
}
