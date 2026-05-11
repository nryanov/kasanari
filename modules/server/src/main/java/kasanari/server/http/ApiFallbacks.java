package kasanari.server.http;

import jakarta.ws.rs.core.Response;

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
}
