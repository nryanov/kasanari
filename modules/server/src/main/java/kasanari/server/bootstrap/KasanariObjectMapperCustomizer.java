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

@Singleton
public class KasanariObjectMapperCustomizer implements ObjectMapperCustomizer {
    @Override
    public void customize(ObjectMapper objectMapper) {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        RESTSerializers.registerAll(objectMapper);
        objectMapper.addMixIn(CreateNamespaceRequest.class, IcebergRestFieldAccessMixin.class);
        objectMapper.addMixIn(CreateTableRequest.class, IcebergRestFieldAccessMixin.class);
        objectMapper.addMixIn(RenameTableRequest.class, IcebergRestFieldAccessMixin.class);
        objectMapper.addMixIn(UpdateNamespacePropertiesRequest.class, IcebergRestFieldAccessMixin.class);
        var icebergRequests = new SimpleModule("kasanari-iceberg-rest");
        icebergRequests.addDeserializer(CreateNamespaceRequest.class, new IcebergCreateNamespaceRequestDeserializer());
        objectMapper.registerModule(icebergRequests);
        PaimonRESTSerializers.registerAll(objectMapper);
    }
}
