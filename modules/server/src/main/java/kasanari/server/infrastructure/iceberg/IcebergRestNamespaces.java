package kasanari.server.infrastructure.iceberg;

import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.rest.RESTUtil;

/**
 * Decodes Iceberg REST path/query namespaces. JAX-RS already URL-decodes path params, so the
 * multipart separator is the unit separator {@code 0x1F}, not {@code '.'}.
 */
public final class IcebergRestNamespaces {
    private IcebergRestNamespaces() {}

    public static Namespace fromPath(String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return Namespace.empty();
        }
        return RESTUtil.namespaceFromQueryParam(namespace);
    }
}
