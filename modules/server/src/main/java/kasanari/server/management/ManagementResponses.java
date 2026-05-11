package kasanari.server.management;

import jakarta.ws.rs.core.Response;
import kasanari.catalog.management.model.ErrorResponse;

public final class ManagementResponses {
    private ManagementResponses() {
    }

    public static Response error(Response.Status status, String message) {
        var error = new ErrorResponse();
        error.setMessage(message);
        return Response.status(status).entity(error).build();
    }
}
