package kasanari.server.infrastructure.http.error;

import jakarta.ws.rs.core.Response;

public record MappedError(Response.Status status, Object entity) {
}
