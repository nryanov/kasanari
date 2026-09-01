package kasanari.server.http.lance;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.NotFoundException;
import kasanari.server.http.CatalogHttpTestSupport;
import org.junit.jupiter.api.Test;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateNamespaceResponse;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceResponse;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.DropNamespaceResponse;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class LanceNamespaceHttpTest extends CatalogHttpTestSupport {
    @Test
    void createNamespaceParsesDottedIdAndOverwritesRequestId() {
        when(lanceAdapter.createNamespace(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new CreateNamespaceResponse().properties(Map.of("owner", "test")));

        givenJson()
                .body("""
                        {"properties":{"owner":"test"},"mode":"create"}
                        """)
                .when()
                .post("/lance/v1/namespace/{id}/create", LANCE_NAMESPACE_ID)
                .then()
                .statusCode(200)
                .body("properties.owner", equalTo("test"));

        var captor = ArgumentCaptor.forClass(CreateNamespaceRequest.class);
        verify(lanceCatalogRouter).getOrThrow(CATALOG);
        verify(lanceAdapter).createNamespace(captor.capture());
        assertEquals(List.of(NAMESPACE), captor.getValue().getId());
        assertEquals("test", captor.getValue().getProperties().get("owner"));
    }

    @Test
    void createNamespaceParsesCustomDelimiter() {
        when(lanceAdapter.createNamespace(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new CreateNamespaceResponse());

        givenJson()
                .queryParam("delimiter", LANCE_CUSTOM_DELIMITER)
                .body("{}")
                .when()
                .post("/lance/v1/namespace/{id}/create", LANCE_NAMESPACE_ID_CUSTOM)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateNamespaceRequest.class);
        verify(lanceAdapter).createNamespace(captor.capture());
        assertEquals(List.of(NAMESPACE), captor.getValue().getId());
    }

    @Test
    void listNamespacesParsesQueryParams() {
        when(lanceAdapter.listNamespaces(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ListNamespacesResponse().namespaces(java.util.Set.of("child")).pageToken("next"));

        givenJson()
                .queryParam("page_token", "tok")
                .queryParam("limit", 12)
                .when()
                .get("/lance/v1/namespace/{id}/list", LANCE_NAMESPACE_ID)
                .then()
                .statusCode(200)
                .body("namespaces", hasItem("child"));

        var captor = ArgumentCaptor.forClass(ListNamespacesRequest.class);
        verify(lanceAdapter).listNamespaces(captor.capture());
        assertEquals(List.of(NAMESPACE), captor.getValue().getId());
        assertEquals("tok", captor.getValue().getPageToken());
        assertEquals(12, captor.getValue().getLimit());
    }

    @Test
    void describeNamespaceParsesBody() {
        when(lanceAdapter.describeNamespace(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DescribeNamespaceResponse());

        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/namespace/{id}/describe", LANCE_NAMESPACE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(DescribeNamespaceRequest.class);
        verify(lanceAdapter).describeNamespace(captor.capture());
        assertEquals(List.of(NAMESPACE), captor.getValue().getId());
    }

    @Test
    void dropNamespaceParsesBody() {
        when(lanceAdapter.dropNamespace(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DropNamespaceResponse());

        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/namespace/{id}/drop", LANCE_NAMESPACE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(DropNamespaceRequest.class);
        verify(lanceAdapter).dropNamespace(captor.capture());
        assertEquals(List.of(NAMESPACE), captor.getValue().getId());
    }

    @Test
    void namespaceExistsParsesBody() {
        doNothing().when(lanceAdapter).namespaceExists(org.mockito.ArgumentMatchers.any());

        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/namespace/{id}/exists", LANCE_NAMESPACE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(NamespaceExistsRequest.class);
        verify(lanceAdapter).namespaceExists(captor.capture());
        assertEquals(List.of(NAMESPACE), captor.getValue().getId());
    }

    @Test
    void listTablesParsesIncludeDeclared() {
        when(lanceAdapter.listTables(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ListTablesResponse().tables(java.util.Set.of(TABLE)));

        givenJson()
                .queryParam("page_token", "t")
                .queryParam("limit", 4)
                .queryParam("include_declared", false)
                .when()
                .get("/lance/v1/namespace/{id}/table/list", LANCE_NAMESPACE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(ListTablesRequest.class);
        verify(lanceAdapter).listTables(captor.capture());
        assertEquals(List.of(NAMESPACE), captor.getValue().getId());
        assertEquals("t", captor.getValue().getPageToken());
        assertEquals(4, captor.getValue().getLimit());
        assertEquals(Boolean.FALSE, captor.getValue().getIncludeDeclared());
    }

    @Test
    void invalidNamespaceIdMapsToBadRequest() {
        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/namespace/{id}/create", "only-one-segment")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("code", equalTo(400))
                .body("detail", equalTo("IllegalArgumentException"));
    }

    @Test
    void unknownCatalogMapsToLanceNotFoundError() {
        when(lanceCatalogRouter.getOrThrow("missing")).thenThrow(new NotFoundException("Lance catalog wasn't found"));

        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/namespace/{id}/create", "missing.NS_0")
                .then()
                .statusCode(404)
                .body("code", equalTo(404))
                .body("detail", equalTo("NotFoundException"));
    }
}
