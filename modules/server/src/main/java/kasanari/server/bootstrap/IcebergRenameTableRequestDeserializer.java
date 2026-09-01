package kasanari.server.bootstrap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.RenameTableRequest;

import java.io.IOException;

public final class IcebergRenameTableRequestDeserializer extends JsonDeserializer<RenameTableRequest> {
    @Override
    public RenameTableRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }

        var builder = RenameTableRequest.builder();
        var source = node.get("source");
        if (source != null && !source.isNull()) {
            builder.withSource(parser.getCodec().treeToValue(source, TableIdentifier.class));
        }
        var destination = node.get("destination");
        if (destination != null && !destination.isNull()) {
            builder.withDestination(parser.getCodec().treeToValue(destination, TableIdentifier.class));
        }
        return builder.build();
    }
}
