package kasanari.server.bootstrap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.iceberg.PartitionSpecParser;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrderParser;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;

public final class IcebergCreateTableRequestDeserializer extends JsonDeserializer<CreateTableRequest> {
    @Override
    public CreateTableRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }

        var builder = CreateTableRequest.builder();
        if (node.hasNonNull("name")) {
            builder.withName(node.get("name").asText());
        }
        if (node.hasNonNull("location")) {
            builder.withLocation(node.get("location").asText());
        }

        var schema = parser.getCodec().treeToValue(node.get("schema"), Schema.class);
        if (schema != null) {
            builder.withSchema(schema);
            var specNode = first(node, "partition-spec", "partitionSpec");
            if (specNode != null) {
                builder.withPartitionSpec(PartitionSpecParser.fromJson(schema, specNode));
            }
            var orderNode = first(node, "write-order", "writeOrder");
            if (orderNode != null) {
                builder.withWriteOrder(SortOrderParser.fromJson(schema, orderNode));
            }
        }

        if (node.hasNonNull("properties")) {
            builder.setProperties(new HashMap<>(JsonUtil.getStringMap("properties", node)));
        }
        var stageCreate = first(node, "stage-create", "stageCreate");
        if (stageCreate != null && stageCreate.asBoolean()) {
            builder.stageCreate();
        }
        return builder.build();
    }

    private static JsonNode first(JsonNode node, String kebab, String camel) {
        var value = node.get(kebab);
        if (value != null && !value.isNull()) {
            return value;
        }
        value = node.get(camel);
        return value == null || value.isNull() ? null : value;
    }
}
