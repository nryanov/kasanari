package kasanari.server.infrastructure.http.error;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import kasanari.catalog.iceberg.exception.IcebergCatalogAdapterException;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.exceptions.NoSuchViewException;
import org.apache.iceberg.exceptions.NamespaceNotEmptyException;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.rest.responses.ErrorResponse;

public final class IcebergErrorTranslator {
    private static final String INTERNAL_ERROR_TYPE = "InternalServerError";

    private IcebergErrorTranslator() {
    }

    public static MappedError translate(Throwable throwable) {
        Throwable cause = ThrowableUnwrapper.unwrap(throwable);

        if (cause instanceof NotFoundException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage(), NotFoundException.class.getSimpleName());
        }
        if (cause instanceof NoSuchNamespaceException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof NoSuchTableException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof NoSuchViewException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof org.apache.iceberg.exceptions.NotFoundException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof AlreadyExistsException e) {
            return mapped(Response.Status.CONFLICT, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof NamespaceNotEmptyException e) {
            return mapped(Response.Status.CONFLICT, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof CommitFailedException e) {
            return mapped(Response.Status.CONFLICT, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof ValidationException e) {
            return mapped(Response.Status.BAD_REQUEST, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof IllegalArgumentException e) {
            return mapped(Response.Status.BAD_REQUEST, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof IcebergCatalogAdapterException.UnsupportedMethod e) {
            return mapped(Response.Status.NOT_IMPLEMENTED, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof UnsupportedOperationException e) {
            return mapped(Response.Status.NOT_IMPLEMENTED, e.getMessage(), cause.getClass().getSimpleName());
        }

        return mapped(
                Response.Status.INTERNAL_SERVER_ERROR,
                cause.getMessage(),
                INTERNAL_ERROR_TYPE);
    }

    private static MappedError mapped(Response.Status status, String message, String type) {
        var body = ErrorResponse.builder()
                .withMessage(message)
                .withType(type)
                .responseCode(status.getStatusCode())
                .build();

        return new MappedError(status, body);
    }
}
