package kasanari.server.infrastructure.http.error;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import kasanari.catalog.management.dto.ErrorResponseDto;

public final class ManagementErrorTranslator {
    private static final String INTERNAL_ERROR_MESSAGE = "Internal server error";

    private ManagementErrorTranslator() {
    }

    public static MappedError translate(Throwable throwable) {
        Throwable cause = ThrowableUnwrapper.unwrap(throwable);

        if (cause instanceof NotFoundException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage());
        }
        if (cause instanceof IllegalArgumentException e) {
            return mapped(Response.Status.BAD_REQUEST, e.getMessage());
        }
        if (cause instanceof UnsupportedOperationException e) {
            return mapped(Response.Status.NOT_IMPLEMENTED, e.getMessage());
        }

        return mapped(Response.Status.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_MESSAGE);
    }

    private static MappedError mapped(Response.Status status, String message) {
        var body = new ErrorResponseDto();
        body.setMessage(message);
        return new MappedError(status, body);
    }
}
