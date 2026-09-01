package kasanari.server.http.paimon;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.NotFoundException;
import kasanari.server.http.CatalogHttpTestSupport;
import org.apache.paimon.rest.RESTApi;
import org.apache.paimon.rest.requests.AlterDatabaseRequest;
import org.apache.paimon.rest.requests.CreateDatabaseRequest;
import org.apache.paimon.rest.responses.AlterDatabaseResponse;
import org.apache.paimon.rest.responses.GetDatabaseResponse;
import org.apache.paimon.rest.responses.ListDatabasesResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaimonDatabaseHttpTest extends CatalogHttpTestSupport {
    @Test
    void listDatabasesParsesPagination() throws Exception {
        when(paimonAdapter.listDatabases(25, "tok"))
                .thenReturn(new ListDatabasesResponse(List.of(DATABASE), "next"));

        givenJson()
                .queryParam("maxResults", 25)
                .queryParam("pageToken", "tok")
                .when()
                .get("/paimon/v1/{catalog}/databases", CATALOG)
                .then()
                .statusCode(200)
                .body("databases", hasItem(DATABASE));

        verify(paimonAdapter).listDatabases(25, "tok");
    }

    @Test
    void createDatabaseParsesBody() {
        var request = new CreateDatabaseRequest(DATABASE, Map.of("owner", "test"));

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases", CATALOG)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateDatabaseRequest.class);
        verify(paimonAdapter).createDatabase(captor.capture());
        assertEquals(DATABASE, captor.getValue().getName());
        assertEquals("test", captor.getValue().getOptions().get("owner"));
    }

    @Test
    void getDatabaseParsesPath() throws Exception {
        var response = RESTApi.fromJson(
                "{\"id\":\"1\",\"name\":\"DB_0\",\"location\":\"file:/tmp/DB_0\",\"options\":{}}",
                GetDatabaseResponse.class
        );
        when(paimonAdapter.getDatabase(DATABASE)).thenReturn(response);

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}", CATALOG, DATABASE)
                .then()
                .statusCode(200)
                .body("name", equalTo(DATABASE));

        verify(paimonAdapter).getDatabase(DATABASE);
    }

    @Test
    void dropDatabaseParsesPath() {
        givenJson()
                .when()
                .delete("/paimon/v1/{catalog}/databases/{database}", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).dropDatabase(DATABASE);
    }

    @Test
    void alterDatabaseParsesBody() throws Exception {
        var request = new AlterDatabaseRequest(List.of("owner"), Map.of("owner", "updated"));
        when(paimonAdapter.alterDatabase(eq(DATABASE), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new AlterDatabaseResponse(List.of("owner"), List.of(), List.of()));

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(AlterDatabaseRequest.class);
        verify(paimonAdapter).alterDatabase(eq(DATABASE), captor.capture());
        assertEquals(List.of("owner"), captor.getValue().getRemovals());
        assertEquals("updated", captor.getValue().getUpdates().get("owner"));
    }

    @Test
    void unknownCatalogMapsToPaimonNotFoundError() {
        when(paimonCatalogRouter.getOrThrow("missing")).thenThrow(new NotFoundException("Paimon catalog wasn't found"));

        givenJson()
                .when()
                .get("/paimon/v1/missing/databases")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Paimon catalog wasn't found"))
                .body("code", equalTo(404));
    }
}
