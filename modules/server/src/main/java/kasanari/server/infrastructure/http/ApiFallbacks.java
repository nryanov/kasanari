package kasanari.server.infrastructure.http;

import jakarta.ws.rs.core.Response;
import kasanari.catalog.management.dto.ErrorResponseDto;

import java.util.Map;

public final class ApiFallbacks {
    private ApiFallbacks() {
    }

    public static Response notImplemented(String handler) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(Map.of(
                        "message", "Handler is not implemented yet",
                        "handler", handler
                ))
                .build();
    }

    public static Response error(Response.Status status, String message) {
        var error = new ErrorResponseDto();
        error.setMessage(message);
        return Response.status(status).entity(error).build();
    }
}
