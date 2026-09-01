package kasanari.server.bootstrap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.iceberg.rest.requests.UpdateNamespacePropertiesRequest;
import org.apache.iceberg.util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;

public final class IcebergUpdateNamespacePropertiesRequestDeserializer
        extends JsonDeserializer<UpdateNamespacePropertiesRequest> {
    @Override
    public UpdateNamespacePropertiesRequest deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }

        var builder = UpdateNamespacePropertiesRequest.builder();
        if (node.hasNonNull("updates")) {
            builder.updateAll(new HashMap<>(JsonUtil.getStringMap("updates", node)));
        }
        if (node.hasNonNull("removals")) {
            builder.removeAll(JsonUtil.getStringList("removals", node));
        }
        return builder.build();
    }
}
