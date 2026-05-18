package kasanari.server.bootstrap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.paimon.rest.RESTApi;

import java.io.IOException;

// https://github.com/apache/paimon/issues/6822
public class PaimonRestJacksonBridge {

    private PaimonRestJacksonBridge() {}

    static <T> void register(SimpleModule module, Class<T> type) {
        module.addSerializer(type, new JsonSerializer<T>() {
            @Override
            public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                    return;
                }
                try {
                    gen.writeRawValue(RESTApi.toJson(value));
                } catch (Exception e) {
                    serializers.reportMappingProblem("Failed to serialize " + type.getName(), e);
                }
            }
        });
        module.addDeserializer(type, new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                JsonNode tree = parser.getCodec().readTree(parser);
                if (tree == null || tree.isNull()) {
                    return null;
                }
                try {
                    return type.cast(RESTApi.fromJson(tree.toString(), type));
                } catch (Exception e) {
                    throw context.instantiationException(type, e);
                }
            }
        });
    }
}
