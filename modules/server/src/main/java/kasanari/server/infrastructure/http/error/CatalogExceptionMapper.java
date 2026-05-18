package kasanari.server.infrastructure.http.error;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@ApplicationScoped
public class CatalogExceptionMapper {
    private static final Logger logger = Logger.getLogger(CatalogExceptionMapper.class);

    @ServerExceptionMapper
    public RestResponse<?> mapException(Throwable exception, UriInfo uriInfo) {
        var path = uriInfo == null ? "" : uriInfo.getPath();
        logger.errorf(exception, "Request failed on path %s", path);

        var mapped = translate(path, exception);
        return RestResponse.status(mapped.status(), mapped.entity());
    }

    private static MappedError translate(String path, Throwable exception) {
        var normalized = normalizePath(path);

        if (normalized.startsWith("/iceberg/")) {
            return IcebergErrorTranslator.translate(exception);
        }

        if (normalized.startsWith("/paimon/")) {
            return PaimonErrorTranslator.translate(exception);
        }

        if (normalized.startsWith("/lance/")) {
            return LanceErrorTranslator.translate(exception);
        }

        return ManagementErrorTranslator.translate(exception);
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
