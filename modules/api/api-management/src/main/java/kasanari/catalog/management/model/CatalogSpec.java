package kasanari.catalog.management.model;

public class CatalogSpec {
    public enum TypeEnum {
        ICEBERG("ICEBERG"),
        PAIMON("PAIMON"),
        LANCE("LANCE");

        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        public static TypeEnum fromValue(String value) {
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

    private TypeEnum type;
    private IcebergCatalogSpecModeConfig modeConfig;

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public IcebergCatalogSpecModeConfig getModeConfig() {
        return modeConfig;
    }

    public void setModeConfig(IcebergCatalogSpecModeConfig modeConfig) {
        this.modeConfig = modeConfig;
    }
}
