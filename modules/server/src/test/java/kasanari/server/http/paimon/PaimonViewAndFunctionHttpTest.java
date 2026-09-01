package kasanari.server.http.paimon;

import io.quarkus.test.junit.QuarkusTest;
import kasanari.server.http.CatalogHttpTestSupport;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.function.FunctionDefinition;
import org.apache.paimon.rest.RESTApi;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.AlterViewRequest;
import org.apache.paimon.rest.requests.CreateFunctionRequest;
import org.apache.paimon.rest.requests.CreateViewRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;
import org.apache.paimon.rest.responses.GetFunctionResponse;
import org.apache.paimon.rest.responses.GetViewResponse;
import org.apache.paimon.rest.responses.ListFunctionDetailsResponse;
import org.apache.paimon.rest.responses.ListFunctionsGloballyResponse;
import org.apache.paimon.rest.responses.ListFunctionsResponse;
import org.apache.paimon.rest.responses.ListViewDetailsResponse;
import org.apache.paimon.rest.responses.ListViewsGloballyResponse;
import org.apache.paimon.rest.responses.ListViewsResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaimonViewAndFunctionHttpTest extends CatalogHttpTestSupport {
    @Test
    void listViewsParsesQueryParams() {
        when(paimonAdapter.listViews(DATABASE, 7, "v", "V_%"))
                .thenReturn(new ListViewsResponse(List.of(VIEW), null));

        givenJson()
                .queryParam("maxResults", 7)
                .queryParam("pageToken", "v")
                .queryParam("viewNamePattern", "V_%")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/views", CATALOG, DATABASE)
                .then()
                .statusCode(200)
                .body("views", hasItem(VIEW));

        verify(paimonAdapter).listViews(DATABASE, 7, "v", "V_%");
    }

    @Test
    void createViewParsesBody() {
        var request = new CreateViewRequest(Identifier.create(DATABASE, VIEW), PaimonRequestBodies.viewSchema());

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/views", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateViewRequest.class);
        verify(paimonAdapter).createView(eq(DATABASE), captor.capture());
        assertEquals(VIEW, captor.getValue().getIdentifier().getObjectName());
    }

    @Test
    void listViewDetailsParsesQueryParams() {
        when(paimonAdapter.listViewDetails(DATABASE, 3, "vd", "V_%"))
                .thenReturn(new ListViewDetailsResponse(List.of(), null));

        givenJson()
                .queryParam("maxResults", 3)
                .queryParam("pageToken", "vd")
                .queryParam("viewNamePattern", "V_%")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/view-details", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listViewDetails(DATABASE, 3, "vd", "V_%");
    }

    @Test
    void listViewsGloballyParsesQueryParams() {
        when(paimonAdapter.listViewsGlobally("DB_%", "V_%", 6, "vg"))
                .thenReturn(new ListViewsGloballyResponse(List.of(), null));

        givenJson()
                .queryParam("databaseNamePattern", "DB_%")
                .queryParam("viewNamePattern", "V_%")
                .queryParam("maxResults", 6)
                .queryParam("pageToken", "vg")
                .when()
                .get("/paimon/v1/{catalog}/views", CATALOG)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listViewsGlobally("DB_%", "V_%", 6, "vg");
    }

    @Test
    void getViewParsesPath() throws Exception {
        when(paimonAdapter.getView(DATABASE, VIEW))
                .thenReturn(RESTApi.fromJson("{\"name\":\"V_0\"}", GetViewResponse.class));

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/views/{view}", CATALOG, DATABASE, VIEW)
                .then()
                .statusCode(200);

        verify(paimonAdapter).getView(DATABASE, VIEW);
    }

    @Test
    void alterViewParsesBody() {
        var request = new AlterViewRequest(Collections.emptyList());

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/views/{view}", CATALOG, DATABASE, VIEW)
                .then()
                .statusCode(200);

        verify(paimonAdapter).alterView(eq(DATABASE), eq(VIEW), org.mockito.ArgumentMatchers.any(AlterViewRequest.class));
    }

    @Test
    void dropViewParsesPath() {
        givenJson()
                .when()
                .delete("/paimon/v1/{catalog}/databases/{database}/views/{view}", CATALOG, DATABASE, VIEW)
                .then()
                .statusCode(200);

        verify(paimonAdapter).dropView(DATABASE, VIEW);
    }

    @Test
    void renameViewParsesBody() {
        var request = new RenameTableRequest(Identifier.create(DATABASE, VIEW), Identifier.create(DATABASE, "V_1"));

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/views/rename", CATALOG)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RenameTableRequest.class);
        verify(paimonAdapter).renameView(captor.capture());
        assertEquals(VIEW, captor.getValue().getSource().getObjectName());
        assertEquals("V_1", captor.getValue().getDestination().getObjectName());
    }

    @Test
    void listFunctionsParsesQueryParams() {
        when(paimonAdapter.listFunctions(DATABASE, 9, "f", "FN_%"))
                .thenReturn(new ListFunctionsResponse(List.of(FUNCTION), null));

        givenJson()
                .queryParam("maxResults", 9)
                .queryParam("pageToken", "f")
                .queryParam("functionNamePattern", "FN_%")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/functions", CATALOG, DATABASE)
                .then()
                .statusCode(200)
                .body("functions", hasItem(FUNCTION));

        verify(paimonAdapter).listFunctions(DATABASE, 9, "f", "FN_%");
    }

    @Test
    void createFunctionParsesBody() {
        var request = new CreateFunctionRequest(
                FUNCTION,
                Collections.emptyList(),
                Collections.emptyList(),
                true,
                Map.of("sql", FunctionDefinition.sql("SELECT 1")),
                "test function",
                Collections.emptyMap()
        );

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/functions", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateFunctionRequest.class);
        verify(paimonAdapter).createFunction(eq(DATABASE), captor.capture());
        assertEquals(FUNCTION, captor.getValue().name());
    }

    @Test
    void listFunctionDetailsParsesQueryParams() {
        when(paimonAdapter.listFunctionDetails(DATABASE, 1, "fd", "FN_%"))
                .thenReturn(new ListFunctionDetailsResponse(List.of(), null));

        givenJson()
                .queryParam("maxResults", 1)
                .queryParam("pageToken", "fd")
                .queryParam("functionNamePattern", "FN_%")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/function-details", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listFunctionDetails(DATABASE, 1, "fd", "FN_%");
    }

    @Test
    void listFunctionsGloballyParsesQueryParams() {
        when(paimonAdapter.listFunctionsGlobally("DB_%", "FN_%", 2, "fg"))
                .thenReturn(new ListFunctionsGloballyResponse(List.of(), null));

        givenJson()
                .queryParam("databaseNamePattern", "DB_%")
                .queryParam("functionNamePattern", "FN_%")
                .queryParam("maxResults", 2)
                .queryParam("pageToken", "fg")
                .when()
                .get("/paimon/v1/{catalog}/functions", CATALOG)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listFunctionsGlobally("DB_%", "FN_%", 2, "fg");
    }

    @Test
    void getFunctionParsesPath() throws Exception {
        when(paimonAdapter.getFunction(DATABASE, FUNCTION))
                .thenReturn(RESTApi.fromJson("{\"name\":\"FN_0\"}", GetFunctionResponse.class));

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/functions/{function}", CATALOG, DATABASE, FUNCTION)
                .then()
                .statusCode(200);

        verify(paimonAdapter).getFunction(DATABASE, FUNCTION);
    }

    @Test
    void alterFunctionParsesBody() {
        var request = new AlterFunctionRequest(Collections.emptyList());

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/functions/{function}", CATALOG, DATABASE, FUNCTION)
                .then()
                .statusCode(200);

        verify(paimonAdapter).alterFunction(eq(DATABASE), eq(FUNCTION), org.mockito.ArgumentMatchers.any(AlterFunctionRequest.class));
    }

    @Test
    void dropFunctionParsesPath() {
        givenJson()
                .when()
                .delete("/paimon/v1/{catalog}/databases/{database}/functions/{function}", CATALOG, DATABASE, FUNCTION)
                .then()
                .statusCode(200);

        verify(paimonAdapter).dropFunction(DATABASE, FUNCTION);
    }
}
