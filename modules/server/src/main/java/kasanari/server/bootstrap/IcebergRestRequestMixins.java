package kasanari.server.bootstrap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.iceberg.rest.RESTSerializers;

/**
 * Mixins that force field-based access and explicit deserializers for Iceberg REST request types
 * whose {@code foo()} accessors are treated as read-only getters.
 */
final class IcebergRestRequestMixins {
    private IcebergRestRequestMixins() {}

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    @JsonDeserialize(using = IcebergCreateNamespaceRequestDeserializer.class)
    abstract static class CreateNamespace {}

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    @JsonDeserialize(using = IcebergCreateTableRequestDeserializer.class)
    abstract static class CreateTable {}

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    @JsonDeserialize(using = IcebergRenameTableRequestDeserializer.class)
    abstract static class RenameTable {}

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    @JsonDeserialize(using = IcebergUpdateNamespacePropertiesRequestDeserializer.class)
    abstract static class UpdateNamespaceProperties {}

    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    @JsonDeserialize(using = RESTSerializers.UpdateTableRequestDeserializer.class)
    abstract static class UpdateTable {}
}
