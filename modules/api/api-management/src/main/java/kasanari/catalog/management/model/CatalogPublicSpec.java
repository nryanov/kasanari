package kasanari.catalog.management.model;


public class CatalogPublicSpec {
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
            throw new IllegalArgumentException("Unknown public spec type: " + value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private TypeEnum type;
    private IcebergCatalogPublicSpecModeConfig modeConfig;

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public IcebergCatalogPublicSpecModeConfig getModeConfig() {
        return modeConfig;
    }

    public void setModeConfig(IcebergCatalogPublicSpecModeConfig modeConfig) {
        this.modeConfig = modeConfig;
    }
}
