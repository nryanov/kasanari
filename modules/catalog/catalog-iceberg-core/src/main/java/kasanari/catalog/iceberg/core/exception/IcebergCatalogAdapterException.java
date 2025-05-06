package kasanari.catalog.iceberg.core.exception;

public abstract sealed class IcebergCatalogAdapterException extends RuntimeException
        permits IcebergCatalogAdapterException.UnsupportedMethod {
    public IcebergCatalogAdapterException(String message) {
        super(message);
    }

    public static final class UnsupportedMethod extends IcebergCatalogAdapterException {
        public UnsupportedMethod(String message) {
            super(message);
        }

        public static UnsupportedMethod view(String method) {
            return new UnsupportedMethod(String.format("View method `%s` is not supported in current catalog", method));
        }

        public static UnsupportedMethod namespace(String method) {
            return new UnsupportedMethod(String.format("Namespace method `%s` is not supported in current catalog", method));
        }
    }
}
