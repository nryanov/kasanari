package kasanari.server.infrastructure.http.error;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.rest.responses.ErrorResponse;
import org.apache.paimon.utils.SnapshotNotExistException;

public final class PaimonErrorTranslator {
    private PaimonErrorTranslator() {
    }

    public static MappedError translate(Throwable throwable) {
        var cause = ThrowableUnwrapper.unwrap(throwable);

        if (cause instanceof NotFoundException e) {
            return mapped(Response.Status.NOT_FOUND, e.getMessage(), null, null);
        }
        if (cause instanceof Catalog.DatabaseNotExistException e) {
            return mapped(
                    Response.Status.NOT_FOUND,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_DATABASE,
                    e.database());
        }
        if (cause instanceof Catalog.TableNotExistException e) {
            return mapped(
                    Response.Status.NOT_FOUND,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_TABLE,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.ViewNotExistException e) {
            return mapped(
                    Response.Status.NOT_FOUND,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_VIEW,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.BranchNotExistException e) {
            return mapped(
                    Response.Status.NOT_FOUND,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_BRANCH,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.TagNotExistException e) {
            return mapped(
                    Response.Status.NOT_FOUND,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_TAG,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.FunctionNotExistException e) {
            return mapped(
                    Response.Status.NOT_FOUND,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_FUNCTION,
                    resourceName(e.identifier()));
        }
        if (cause instanceof SnapshotNotExistException e) {
            return mapped(
                    Response.Status.NOT_FOUND,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_SNAPSHOT,
                    null);
        }
        if (cause instanceof Catalog.DatabaseAlreadyExistException e) {
            return mapped(
                    Response.Status.CONFLICT,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_DATABASE,
                    e.database());
        }
        if (cause instanceof Catalog.TableAlreadyExistException e) {
            return mapped(
                    Response.Status.CONFLICT,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_TABLE,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.ViewAlreadyExistException e) {
            return mapped(
                    Response.Status.CONFLICT,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_VIEW,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.BranchAlreadyExistException e) {
            return mapped(
                    Response.Status.CONFLICT,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_BRANCH,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.TagAlreadyExistException e) {
            return mapped(
                    Response.Status.CONFLICT,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_TAG,
                    resourceName(e.identifier()));
        }
        if (cause instanceof Catalog.FunctionAlreadyExistException e) {
            return mapped(
                    Response.Status.CONFLICT,
                    e.getMessage(),
                    ErrorResponse.RESOURCE_TYPE_FUNCTION,
                    resourceName(e.identifier()));
        }
        if (cause instanceof IllegalArgumentException e) {
            return mapped(Response.Status.BAD_REQUEST, e.getMessage(), null, null);
        }
        if (cause instanceof UnsupportedOperationException e) {
            return mapped(Response.Status.NOT_IMPLEMENTED, e.getMessage(), null, null);
        }

        return mapped(Response.Status.INTERNAL_SERVER_ERROR, cause.getMessage(), null, null);
    }

    private static String resourceName(Identifier identifier) {
        return identifier == null ? null : identifier.getFullName();
    }

    private static MappedError mapped(
            Response.Status status, String message, String resourceType, String resourceName) {
        var body = new ErrorResponse(resourceType, resourceName, message, status.getStatusCode());
        return new MappedError(status, body);
    }
}
