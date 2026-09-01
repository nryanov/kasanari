package kasanari.server.bootstrap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;

/**
 * Iceberg {@code RESTSerializers} registers a {@link Namespace} deserializer but not one for
 * {@link CreateNamespaceRequest}. Jackson then uses the no-arg constructor and never binds
 * {@code namespace} (there is no setter; {@code namespace()} is not a JavaBean getter), so the
 * field stays null.
 */
public final class IcebergCreateNamespaceRequestDeserializer extends JsonDeserializer<CreateNamespaceRequest> {
    @Override
    public CreateNamespaceRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }

        var builder = CreateNamespaceRequest.builder();
        var namespaceNode = node.get("namespace");
        if (namespaceNode != null && !namespaceNode.isNull()) {
            builder.withNamespace(Namespace.of(JsonUtil.getStringArray(namespaceNode)));
        }
        if (node.hasNonNull("properties")) {
            builder.setProperties(new HashMap<>(JsonUtil.getStringMap("properties", node)));
        }
        return builder.build();
    }
}
