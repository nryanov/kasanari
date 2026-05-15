package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.management.api.ManagementRestCatalogsService;
import kasanari.catalog.management.dto.CatalogPublicInfoDto;
import kasanari.catalog.management.dto.CatalogTypeDto;
import kasanari.catalog.management.dto.CreateCatalogRequestDto;
import kasanari.catalog.management.dto.UpdateCatalogRequestDto;
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
    public Response createCatalog(CreateCatalogRequestDto createCatalogRequest, SecurityContext securityContext) {
        if (createCatalogRequest == null
                || createCatalogRequest.getCatalogId() == null
                || createCatalogRequest.getCatalogType() == null
                || createCatalogRequest.getMode() == null
                || createCatalogRequest.getSpec() == null) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Catalog payload is incomplete");
        }

        var spec = CatalogSpecMapper.toDomain(createCatalogRequest.getSpec());
        var metadata = new CatalogMetadata(
                createCatalogRequest.getCatalogId(),
                CatalogSpecMapper.toDomain(createCatalogRequest.getCatalogType()),
                CatalogSpecMapper.toDomain(createCatalogRequest.getMode()),
                spec,
                1L
        );
        var created = catalogService.create(metadata);

        if (created) {
            return Response.status(Response.Status.CREATED).entity(toPublicInfo(metadata)).build();
        } else {
            return ApiFallbacks.error(Response.Status.CONFLICT, "Catalog already exists");
        }
    }

    @Override
    public Response deleteCatalog(CatalogTypeDto catalogType, String name, SecurityContext securityContext) {
        var deleted = catalogService.delete(CatalogSpecMapper.toDomain(catalogType), name);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        }
    }

    @Override
    public Response getCatalog(CatalogTypeDto catalogType, String name, SecurityContext securityContext) {
        var maybe = catalogService.get(CatalogSpecMapper.toDomain(catalogType), name);

        if (maybe.isEmpty()) {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        } else {
            return Response.status(Response.Status.OK).entity(toPublicInfo(maybe.get())).build();
        }
    }

    @Override
    public Response updateCatalog(CatalogTypeDto catalogType, String catalogId, UpdateCatalogRequestDto updateCatalogRequest, SecurityContext securityContext) {
        if (updateCatalogRequest == null || updateCatalogRequest.getSpec() == null) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Catalog update requires spec");
        }

        var domainType = CatalogSpecMapper.toDomain(catalogType);
        var existing = catalogService.get(domainType, catalogId);
        if (existing.isEmpty()) {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        }

        var spec = CatalogSpecMapper.toDomain(updateCatalogRequest.getSpec());

        try {
            var updated = catalogService.update(domainType, catalogId, spec, updateCatalogRequest.getExpectedVersion());

            if (updated.isEmpty()) {
                return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
            }

            return Response.status(Response.Status.OK).entity(toPublicInfo(updated.get())).build();
        } catch (IllegalStateException e) {
            return ApiFallbacks.error(Response.Status.CONFLICT, e.getMessage());
        }
    }

    private CatalogPublicInfoDto toPublicInfo(CatalogMetadata metadata) {
        var info = new CatalogPublicInfoDto();
        info.setCatalogId(metadata.catalogId());
        info.setCatalogType(CatalogSpecMapper.toApi(metadata.catalogType()));
        info.setMode(CatalogSpecMapper.toApi(metadata.catalogMode()));
        info.setSpec(CatalogSpecMapper.toApi(metadata.spec()));
        info.setVersion(metadata.version());
        return info;
    }
}
