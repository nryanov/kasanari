package kasanari.server.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.management.api.ManagementRestCatalogsService;
import kasanari.catalog.management.model.CatalogPublicInfo;
import kasanari.catalog.management.model.CreateCatalogRequest;
import kasanari.catalog.management.model.UpdateCatalogRequest;

import java.util.List;

@ApplicationScoped
public class ManagementCatalogService implements ManagementRestCatalogsService {
    private final ManagementInfrastructure infrastructure;
    private final ManagementAuthorizationService authz;

    public ManagementCatalogService(ManagementInfrastructure infrastructure, ManagementAuthorizationService authz) {
        this.infrastructure = infrastructure;
        this.authz = authz;
    }

    @Override
    public Response createCatalog(CreateCatalogRequest createCatalogRequest, SecurityContext securityContext) {
        if (createCatalogRequest == null
                || createCatalogRequest.getCatalogId() == null
                || createCatalogRequest.getCatalogType() == null
                || createCatalogRequest.getMode() == null
                || createCatalogRequest.getSpec() == null) {
            return ManagementResponses.error(Response.Status.BAD_REQUEST, "Catalog payload is incomplete");
        }

        var subject = authz.subject(securityContext);
        if (!authz.canCatalogWrite(subject, createCatalogRequest.getCatalogType(), "create")) {
            return ManagementResponses.error(Response.Status.FORBIDDEN, "Missing permission to create catalog");
        }

        try {
            CatalogSpecMapper.validate(createCatalogRequest.getCatalogType(), createCatalogRequest.getMode(), createCatalogRequest.getSpec());
        } catch (IllegalArgumentException e) {
            return ManagementResponses.error(Response.Status.BAD_REQUEST, e.getMessage());
        }

        var sanitizedSpec = CatalogSpecMapper.toSanitized(createCatalogRequest.getSpec());
        var secrets = CatalogSpecMapper.extractSecrets(createCatalogRequest.getSpec());

        var created = infrastructure.txManager().inTransactionR(tx -> {
            var metadata = new CatalogMetadata(
                    createCatalogRequest.getCatalogId(),
                    createCatalogRequest.getCatalogType(),
                    createCatalogRequest.getMode(),
                    sanitizedSpec,
                    1L
            );

            if (!infrastructure.catalogRepository().create(tx, metadata)) {
                return null;
            }

            infrastructure.catalogRepository().replaceSecrets(tx, metadata.catalogId(), secrets);
            var keys = infrastructure.catalogRepository().getSecretKeys(tx, metadata.catalogId());
            return toPublicInfo(metadata, keys);
        });

        if (created == null) {
            return ManagementResponses.error(Response.Status.CONFLICT, "Catalog already exists");
        }

        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @Override
    public Response deleteCatalog(String catalogId, SecurityContext securityContext) {
        var existing = infrastructure.txManager().inTransactionR(tx -> infrastructure.catalogRepository().getById(tx, catalogId));
        if (existing.isEmpty()) {
            return ManagementResponses.error(Response.Status.NOT_FOUND, "Catalog not found");
        }

        var subject = authz.subject(securityContext);
        if (!authz.canCatalogWrite(subject, existing.get().catalogType(), "delete")) {
            return ManagementResponses.error(Response.Status.FORBIDDEN, "Missing permission to delete catalog");
        }

        infrastructure.txManager().inTransaction(tx -> infrastructure.catalogRepository().delete(tx, catalogId));
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getCatalog(String catalogId, SecurityContext securityContext) {
        var result = infrastructure.txManager().inTransactionR(tx -> {
            var metadata = infrastructure.catalogRepository().getById(tx, catalogId);
            if (metadata.isEmpty()) {
                return null;
            }

            var secretKeys = infrastructure.catalogRepository().getSecretKeys(tx, catalogId);
            return toPublicInfo(metadata.get(), secretKeys);
        });

        if (result == null) {
            return ManagementResponses.error(Response.Status.NOT_FOUND, "Catalog not found");
        }

        var subject = authz.subject(securityContext);
        if (!authz.canCatalogRead(subject, result.getCatalogType())) {
            return ManagementResponses.error(Response.Status.FORBIDDEN, "Missing permission to read catalog");
        }

        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response updateCatalog(String catalogId, UpdateCatalogRequest updateCatalogRequest, SecurityContext securityContext) {
        if (updateCatalogRequest == null || updateCatalogRequest.getSpec() == null) {
            return ManagementResponses.error(Response.Status.BAD_REQUEST, "Catalog update requires spec");
        }

        var existing = infrastructure.txManager().inTransactionR(tx -> infrastructure.catalogRepository().getById(tx, catalogId));
        if (existing.isEmpty()) {
            return ManagementResponses.error(Response.Status.NOT_FOUND, "Catalog not found");
        }

        var subject = authz.subject(securityContext);
        if (!authz.canCatalogWrite(subject, existing.get().catalogType(), "update")) {
            return ManagementResponses.error(Response.Status.FORBIDDEN, "Missing permission to update catalog");
        }

        try {
            CatalogSpecMapper.validate(existing.get().catalogType(), existing.get().catalogMode(), updateCatalogRequest.getSpec());
        } catch (IllegalArgumentException e) {
            return ManagementResponses.error(Response.Status.BAD_REQUEST, e.getMessage());
        }

        var sanitizedSpec = CatalogSpecMapper.toSanitized(updateCatalogRequest.getSpec());
        var secrets = CatalogSpecMapper.extractSecrets(updateCatalogRequest.getSpec());

        try {
            var updated = infrastructure.txManager().inTransactionR(tx -> {
                var maybeUpdated = infrastructure.catalogRepository().update(
                        tx,
                        catalogId,
                        sanitizedSpec,
                        updateCatalogRequest.getExpectedVersion()
                );
                if (maybeUpdated.isEmpty()) {
                    return null;
                }

                infrastructure.catalogRepository().replaceSecrets(tx, catalogId, secrets);
                var secretKeys = infrastructure.catalogRepository().getSecretKeys(tx, catalogId);
                return toPublicInfo(maybeUpdated.get(), secretKeys);
            });

            if (updated == null) {
                return ManagementResponses.error(Response.Status.NOT_FOUND, "Catalog not found");
            }

            return Response.status(Response.Status.OK).entity(updated).build();
        } catch (IllegalStateException e) {
            return ManagementResponses.error(Response.Status.CONFLICT, e.getMessage());
        }
    }

    private CatalogPublicInfo toPublicInfo(CatalogMetadata metadata, List<String> secretKeys) {
        var info = new CatalogPublicInfo();
        info.setCatalogId(metadata.catalogId());
        info.setCatalogType(metadata.catalogType());
        info.setMode(metadata.catalogMode());
        info.setSpec(CatalogSpecMapper.toPublic(metadata.spec()));
        info.setSecretKeys(secretKeys);
        info.setVersion(metadata.version());
        return info;
    }
}
