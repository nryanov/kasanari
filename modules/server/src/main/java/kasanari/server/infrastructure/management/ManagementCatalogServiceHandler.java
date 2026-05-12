package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.management.api.ManagementRestCatalogsService;
import kasanari.catalog.management.model.CatalogPublicInfo;
import kasanari.catalog.management.model.CreateCatalogRequest;
import kasanari.catalog.management.model.UpdateCatalogRequest;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.server.infrastructure.http.ApiFallbacks;

@ApplicationScoped
public class ManagementCatalogServiceHandler implements ManagementRestCatalogsService {
    private final ManagementCatalogService catalogService;

    public ManagementCatalogServiceHandler(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    public Response createCatalog(CreateCatalogRequest createCatalogRequest, SecurityContext securityContext) {
        if (createCatalogRequest == null
                || createCatalogRequest.getCatalogId() == null
                || createCatalogRequest.getCatalogType() == null
                || createCatalogRequest.getMode() == null
                || createCatalogRequest.getSpec() == null) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Catalog payload is incomplete");
        }

        try {
            CatalogSpecMapper.validate(createCatalogRequest.getCatalogType(), createCatalogRequest.getMode(), createCatalogRequest.getSpec());
        } catch (IllegalArgumentException e) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, e.getMessage());
        }

        var spec = CatalogSpecMapper.copy(createCatalogRequest.getSpec());
        var metadata = new CatalogMetadata(createCatalogRequest.getCatalogId(), createCatalogRequest.getCatalogType(), createCatalogRequest.getMode(), spec, 1L);
        var created = catalogService.create(metadata);

        if (created) {
            return Response.status(Response.Status.CREATED).entity(toPublicInfo(metadata)).build();
        } else {
            return ApiFallbacks.error(Response.Status.CONFLICT, "Catalog already exists");
        }
    }

    @Override
    public Response deleteCatalog(String catalogId, SecurityContext securityContext) {
        var deleted = catalogService.delete(catalogId);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        }
    }

    @Override
    public Response getCatalog(String catalogId, SecurityContext securityContext) {
        var maybe = catalogService.get(catalogId);

        if (maybe.isEmpty()) {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        } else {
            return Response.status(Response.Status.OK).entity(toPublicInfo(maybe.get())).build();
        }
    }

    @Override
    public Response updateCatalog(String catalogId, UpdateCatalogRequest updateCatalogRequest, SecurityContext securityContext) {
        if (updateCatalogRequest == null || updateCatalogRequest.getSpec() == null) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Catalog update requires spec");
        }

        var existing = catalogService.get(catalogId);
        if (existing.isEmpty()) {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        }

        try {
            CatalogSpecMapper.validate(existing.get().catalogType(), existing.get().catalogMode(), updateCatalogRequest.getSpec());
        } catch (IllegalArgumentException e) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, e.getMessage());
        }

        var spec = CatalogSpecMapper.copy(updateCatalogRequest.getSpec());

        try {
            var updated = catalogService.update(catalogId, spec, updateCatalogRequest.getExpectedVersion());

            if (updated.isEmpty()) {
                return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
            }

            return Response.status(Response.Status.OK).entity(updated).build();
        } catch (IllegalStateException e) {
            return ApiFallbacks.error(Response.Status.CONFLICT, e.getMessage());
        }
    }

    private CatalogPublicInfo toPublicInfo(CatalogMetadata metadata) {
        var info = new CatalogPublicInfo();
        info.setCatalogId(metadata.catalogId());
        info.setCatalogType(metadata.catalogType());
        info.setMode(metadata.catalogMode());
        info.setSpec(CatalogSpecMapper.copy(metadata.spec()));
        info.setVersion(metadata.version());
        return info;
    }
}
