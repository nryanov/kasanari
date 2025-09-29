package kasanari.catalog.iceberg.kasanari.utils;

import org.apache.iceberg.catalog.Namespace;

public final class IcebergUtils {
    private IcebergUtils() {}

    public static String namespaceName(Namespace namespace) {
        if (namespace.isEmpty()) {
            return null;
        }

        return String.join(".", namespace.levels());
    }
}
