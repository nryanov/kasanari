package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
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
    private final AuthorizationService authorizationService;

    public ManagementCatalogServiceHandler(
            ManagementCatalogService catalogService,
            AuthorizationService authorizationService
    ) {
        this.catalogService = catalogService;
        this.authorizationService = authorizationService;
    }

    @Override
    public Response createCatalog(CreateCatalogRequestDto createCatalogRequest, SecurityContext securityContext) {
        var domainType = CatalogSpecMapper.toDomain(createCatalogRequest.getCatalogType());
        var denied = authorizationService.denyUnless(securityContext, domainType, Permission.catalogCreate(domainType));
        if (denied.isPresent()) {
            return denied.get();
        }

        var spec = CatalogSpecMapper.toDomain(createCatalogRequest.getSpec());
        var metadata = new CatalogMetadata(
                createCatalogRequest.getCatalogId(),
                domainType,
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
        var domainType = CatalogSpecMapper.toDomain(catalogType);
        var denied = authorizationService.denyUnless(securityContext, domainType, Permission.catalogDelete(domainType));
        if (denied.isPresent()) {
            return denied.get();
        }

        var deleted = catalogService.delete(domainType, name);

        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        }
    }

    @Override
    public Response getCatalog(CatalogTypeDto catalogType, String name, SecurityContext securityContext) {
        var domainType = CatalogSpecMapper.toDomain(catalogType);
        var denied = authorizationService.denyUnless(securityContext, domainType, Permission.catalogGet(domainType));
        if (denied.isPresent()) {
            return denied.get();
        }

        var maybe = catalogService.get(domainType, name);

        if (maybe.isEmpty()) {
            return ApiFallbacks.error(Response.Status.NOT_FOUND, "Catalog not found");
        } else {
            return Response.status(Response.Status.OK).entity(toPublicInfo(maybe.get())).build();
        }
    }

    @Override
    public Response updateCatalog(CatalogTypeDto catalogType, String catalogId, UpdateCatalogRequestDto updateCatalogRequest, SecurityContext securityContext) {
        var domainType = CatalogSpecMapper.toDomain(catalogType);
        var denied = authorizationService.denyUnless(securityContext, domainType, Permission.catalogUpdate(domainType));
        if (denied.isPresent()) {
            return denied.get();
        }

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
        info.setCatalogId(metadata.catalogName());
        info.setCatalogType(CatalogSpecMapper.toApi(metadata.catalogType()));
        info.setMode(CatalogSpecMapper.toApi(metadata.catalogMode()));
        info.setSpec(CatalogSpecMapper.toApi(metadata.spec()));
        info.setVersion(metadata.version());
        return info;
    }
}
