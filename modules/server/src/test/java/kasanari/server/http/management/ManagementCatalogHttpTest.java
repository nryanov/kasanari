package kasanari.server.http.management;

import io.quarkus.test.junit.QuarkusTest;
import kasanari.core.model.CatalogType;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.repository.management.catalog.model.CatalogMode;
import kasanari.repository.management.catalog.model.CatalogSpec;
import kasanari.server.http.CatalogHttpTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ManagementCatalogHttpTest extends CatalogHttpTestSupport {
    @Test
    void createCatalogParsesTypeModeAndSpec() {
        when(managementCatalogService.create(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        givenJson()
                .body("""
                        {
                          "catalogId": "C_0",
                          "catalogType": "ICEBERG",
                          "mode": "INTERNAL",
                          "spec": {
                            "fileIoProperties": {"fs.s3a.endpoint": "http://minio:9000"},
                            "catalogProperties": {"warehouse": "s3a://warehouse"}
                          }
                        }
                        """)
                .when()
                .post("/management/v1/catalogs")
                .then()
                .statusCode(201)
                .body("catalogId", equalTo("C_0"))
                .body("catalogType", equalTo("ICEBERG"))
                .body("mode", equalTo("INTERNAL"))
                .body("spec.catalogProperties.warehouse", equalTo("s3a://warehouse"));

        var captor = ArgumentCaptor.forClass(CatalogMetadata.class);
        verify(managementCatalogService).create(captor.capture());
        var metadata = captor.getValue();
        assertEquals("C_0", metadata.catalogName());
        assertEquals(CatalogType.ICEBERG, metadata.catalogType());
        assertEquals(CatalogMode.INTERNAL, metadata.catalogMode());
        assertEquals("http://minio:9000", metadata.spec().fileIoProperties().get("fs.s3a.endpoint"));
        assertEquals("s3a://warehouse", metadata.spec().catalogProperties().get("warehouse"));
        assertEquals(1L, metadata.version());
    }

    @Test
    void createCatalogConflictWhenAlreadyExists() {
        when(managementCatalogService.create(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        givenJson()
                .body("""
                        {
                          "catalogId": "C_0",
                          "catalogType": "PAIMON",
                          "mode": "PROXY",
                          "spec": {"fileIoProperties": {}, "catalogProperties": {}}
                        }
                        """)
                .when()
                .post("/management/v1/catalogs")
                .then()
                .statusCode(409);
    }

    @Test
    void getCatalogParsesTypeAndId() {
        var metadata = new CatalogMetadata(
                CATALOG,
                CatalogType.LANCE,
                CatalogMode.PROXY,
                new CatalogSpec(Map.of("io", "dir"), Map.of("implementation", "dir")),
                3L
        );
        when(managementCatalogService.get(CatalogType.LANCE, CATALOG)).thenReturn(Optional.of(metadata));

        givenJson()
                .when()
                .get("/management/v1/catalogs/{catalogType}/{catalogId}", "LANCE", CATALOG)
                .then()
                .statusCode(200)
                .body("catalogType", equalTo("LANCE"))
                .body("mode", equalTo("PROXY"))
                .body("version", equalTo(3));

        verify(managementCatalogService).get(CatalogType.LANCE, CATALOG);
    }

    @Test
    void getCatalogNotFound() {
        when(managementCatalogService.get(CatalogType.ICEBERG, "missing")).thenReturn(Optional.empty());

        givenJson()
                .when()
                .get("/management/v1/catalogs/{catalogType}/{catalogId}", "ICEBERG", "missing")
                .then()
                .statusCode(404);
    }

    @Test
    void updateCatalogParsesExpectedVersionAndSpec() {
        var existing = new CatalogMetadata(
                CATALOG,
                CatalogType.ICEBERG,
                CatalogMode.INTERNAL,
                new CatalogSpec(Map.of(), Map.of("warehouse", "old")),
                2L
        );
        var updated = new CatalogMetadata(
                CATALOG,
                CatalogType.ICEBERG,
                CatalogMode.INTERNAL,
                new CatalogSpec(Map.of("a", "b"), Map.of("warehouse", "new")),
                3L
        );
        when(managementCatalogService.get(CatalogType.ICEBERG, CATALOG)).thenReturn(Optional.of(existing));
        when(managementCatalogService.update(eq(CatalogType.ICEBERG), eq(CATALOG), org.mockito.ArgumentMatchers.any(), eq(2L)))
                .thenReturn(Optional.of(updated));

        givenJson()
                .body("""
                        {
                          "expectedVersion": 2,
                          "spec": {
                            "fileIoProperties": {"a": "b"},
                            "catalogProperties": {"warehouse": "new"}
                          }
                        }
                        """)
                .when()
                .patch("/management/v1/catalogs/{catalogType}/{catalogId}", "ICEBERG", CATALOG)
                .then()
                .statusCode(200)
                .body("version", equalTo(3))
                .body("spec.catalogProperties.warehouse", equalTo("new"));

        var specCaptor = ArgumentCaptor.forClass(CatalogSpec.class);
        verify(managementCatalogService).update(eq(CatalogType.ICEBERG), eq(CATALOG), specCaptor.capture(), eq(2L));
        assertEquals("b", specCaptor.getValue().fileIoProperties().get("a"));
        assertEquals("new", specCaptor.getValue().catalogProperties().get("warehouse"));
    }

    @Test
    void deleteCatalogParsesTypeAndId() {
        when(managementCatalogService.delete(CatalogType.PAIMON, CATALOG)).thenReturn(true);

        givenJson()
                .when()
                .delete("/management/v1/catalogs/{catalogType}/{catalogId}", "PAIMON", CATALOG)
                .then()
                .statusCode(204);

        verify(managementCatalogService).delete(CatalogType.PAIMON, CATALOG);
    }

    @Test
    void deleteCatalogNotFound() {
        when(managementCatalogService.delete(CatalogType.PAIMON, CATALOG)).thenReturn(false);

        givenJson()
                .when()
                .delete("/management/v1/catalogs/{catalogType}/{catalogId}", "PAIMON", CATALOG)
                .then()
                .statusCode(404);
    }
}
