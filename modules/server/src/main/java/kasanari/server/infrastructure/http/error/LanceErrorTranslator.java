package kasanari.server.infrastructure.http.error;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.lance.namespace.model.ErrorResponse;

public final class LanceErrorTranslator {
    private static final String INTERNAL_ERROR_TYPE = "InternalServerError";

    private LanceErrorTranslator() {
    }

    public static MappedError translate(Throwable throwable) {
        Throwable cause = ThrowableUnwrapper.unwrap(throwable);

        if (cause instanceof NotFoundException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage(), NotFoundException.class.getSimpleName());
        }
        if (cause instanceof IllegalArgumentException e) {
            return mapped(Response.Status.BAD_REQUEST, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof UnsupportedOperationException e) {
            return mapped(Response.Status.NOT_IMPLEMENTED, e.getMessage(), cause.getClass().getSimpleName());
        }
        if (cause instanceof IllegalStateException e && cause.getMessage() != null) {
            var message = e.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("does not exist")) {
                return mapped(Response.Status.NOT_FOUND, e.getMessage(), cause.getClass().getSimpleName());
            }
            if (message.contains("already exist") || message.contains("conflict")) {
                return mapped(Response.Status.CONFLICT, e.getMessage(), cause.getClass().getSimpleName());
            }
        }

        return mapped(
                Response.Status.INTERNAL_SERVER_ERROR,
                cause.getMessage(),
                INTERNAL_ERROR_TYPE);
    }

    private static MappedError mapped(Response.Status status, String message, String type) {
        var body = new ErrorResponse()
                .error(message)
                .code(status.getStatusCode())
                .detail(type);
        return new MappedError(status, body);
    }
}
