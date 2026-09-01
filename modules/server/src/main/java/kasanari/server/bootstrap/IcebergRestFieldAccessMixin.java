package kasanari.server.bootstrap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Iceberg REST request types expose {@code foo()} accessors without JavaBean setters. Jackson then
 * treats those properties as read-only and never binds the matching fields, even when field
 * visibility is {@code ANY}.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class IcebergRestFieldAccessMixin {
}
