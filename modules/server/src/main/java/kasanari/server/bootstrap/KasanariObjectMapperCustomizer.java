package kasanari.server.bootstrap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import org.apache.iceberg.rest.RESTSerializers;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.RenameTableRequest;
import org.apache.iceberg.rest.requests.UpdateNamespacePropertiesRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;

@Singleton
public class KasanariObjectMapperCustomizer implements ObjectMapperCustomizer {
    @Override
    public int priority() {
        return MAXIMUM_PRIORITY;
    }

    @Override
    public void customize(ObjectMapper objectMapper) {
        // Match Iceberg RESTObjectMapper: foo() accessors must not hide private fields.
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        RESTSerializers.registerAll(objectMapper);
        objectMapper.addMixIn(CreateNamespaceRequest.class, IcebergRestRequestMixins.CreateNamespace.class);
        objectMapper.addMixIn(CreateTableRequest.class, IcebergRestRequestMixins.CreateTable.class);
        objectMapper.addMixIn(RenameTableRequest.class, IcebergRestRequestMixins.RenameTable.class);
        objectMapper.addMixIn(UpdateNamespacePropertiesRequest.class, IcebergRestRequestMixins.UpdateNamespaceProperties.class);
        objectMapper.addMixIn(UpdateTableRequest.class, IcebergRestRequestMixins.UpdateTable.class);
        var icebergRequests = new SimpleModule("kasanari-iceberg-rest");
        icebergRequests.addDeserializer(CreateNamespaceRequest.class, new IcebergCreateNamespaceRequestDeserializer());
        icebergRequests.addDeserializer(CreateTableRequest.class, new IcebergCreateTableRequestDeserializer());
        icebergRequests.addDeserializer(RenameTableRequest.class, new IcebergRenameTableRequestDeserializer());
        icebergRequests.addDeserializer(
                UpdateNamespacePropertiesRequest.class,
                new IcebergUpdateNamespacePropertiesRequestDeserializer()
        );
        icebergRequests.addDeserializer(UpdateTableRequest.class, new RESTSerializers.UpdateTableRequestDeserializer());
        objectMapper.registerModule(icebergRequests);
        PaimonRESTSerializers.registerAll(objectMapper);
    }
}
