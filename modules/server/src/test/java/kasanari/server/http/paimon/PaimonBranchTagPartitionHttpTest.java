package kasanari.server.http.paimon;

import io.quarkus.test.junit.QuarkusTest;
import kasanari.server.http.CatalogHttpTestSupport;
import org.apache.paimon.rest.RESTApi;
import org.apache.paimon.rest.requests.CreateBranchRequest;
import org.apache.paimon.rest.requests.CreateTagRequest;
import org.apache.paimon.rest.requests.ForwardBranchRequest;
import org.apache.paimon.rest.requests.ListPartitionsByNamesRequest;
import org.apache.paimon.rest.requests.MarkDonePartitionsRequest;
import org.apache.paimon.rest.requests.RenameBranchRequest;
import org.apache.paimon.rest.responses.GetTagResponse;
import org.apache.paimon.rest.responses.ListBranchesResponse;
import org.apache.paimon.rest.responses.ListPartitionsResponse;
import org.apache.paimon.rest.responses.ListTagsResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaimonBranchTagPartitionHttpTest extends CatalogHttpTestSupport {
    @Test
    void listPartitionsParsesQueryParams() {
        when(paimonAdapter.listPartitions(DATABASE, TABLE, 11, "pt", "dt=%"))
                .thenReturn(new ListPartitionsResponse(List.of(), null));

        givenJson()
                .queryParam("maxResults", 11)
                .queryParam("pageToken", "pt")
                .queryParam("partitionNamePattern", "dt=%")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/partitions", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listPartitions(DATABASE, TABLE, 11, "pt", "dt=%");
    }

    @Test
    void markDonePartitionsParsesBody() {
        var request = new MarkDonePartitionsRequest(List.of(Map.of("dt", "dt_a")));

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/partitions/mark", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(MarkDonePartitionsRequest.class);
        verify(paimonAdapter).markDonePartitions(eq(DATABASE), eq(TABLE), captor.capture());
        assertEquals(List.of(Map.of("dt", "dt_a")), captor.getValue().getPartitionSpecs());
    }

    @Test
    void listPartitionsByNamesParsesBody() {
        var request = new ListPartitionsByNamesRequest(List.of(Map.of("dt", "dt_a")));
        when(paimonAdapter.listPartitionsByNames(eq(DATABASE), eq(TABLE), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ListPartitionsResponse(List.of(), null));

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/partitions/list-by-names", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(ListPartitionsByNamesRequest.class);
        verify(paimonAdapter).listPartitionsByNames(eq(DATABASE), eq(TABLE), captor.capture());
        assertEquals(List.of(Map.of("dt", "dt_a")), captor.getValue().getPartitionSpecs());
    }

    @Test
    void listBranchesParsesPath() {
        when(paimonAdapter.listBranches(DATABASE, TABLE)).thenReturn(new ListBranchesResponse(List.of("main", BRANCH)));

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/branches", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200)
                .body("branches", hasItem(BRANCH));

        verify(paimonAdapter).listBranches(DATABASE, TABLE);
    }

    @Test
    void createBranchParsesBody() {
        var request = new CreateBranchRequest(BRANCH, null);

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/branches", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateBranchRequest.class);
        verify(paimonAdapter).createBranch(eq(DATABASE), eq(TABLE), captor.capture());
        assertEquals(BRANCH, captor.getValue().branch());
    }

    @Test
    void dropBranchParsesPath() {
        givenJson()
                .when()
                .delete("/paimon/v1/{catalog}/databases/{database}/tables/{table}/branches/{branch}", CATALOG, DATABASE, TABLE, BRANCH)
                .then()
                .statusCode(200);

        verify(paimonAdapter).dropBranch(DATABASE, TABLE, BRANCH);
    }

    @Test
    void renameBranchParsesBody() {
        var request = new RenameBranchRequest("BR_1");

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/branches/{branch}/rename", CATALOG, DATABASE, TABLE, BRANCH)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RenameBranchRequest.class);
        verify(paimonAdapter).renameBranch(eq(DATABASE), eq(TABLE), eq(BRANCH), captor.capture());
        assertEquals("BR_1", captor.getValue().toBranch());
    }

    @Test
    void forwardBranchParsesBody() {
        var request = new ForwardBranchRequest();

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/branches/{branch}/forward", CATALOG, DATABASE, TABLE, BRANCH)
                .then()
                .statusCode(200);

        verify(paimonAdapter).forwardBranch(eq(DATABASE), eq(TABLE), eq(BRANCH), org.mockito.ArgumentMatchers.any(ForwardBranchRequest.class));
    }

    @Test
    void listTagsParsesQueryParams() {
        when(paimonAdapter.listTags(DATABASE, TABLE, 5, "tt", "TAG_"))
                .thenReturn(new ListTagsResponse(List.of(TAG), null));

        givenJson()
                .queryParam("maxResults", 5)
                .queryParam("pageToken", "tt")
                .queryParam("tagNamePrefix", "TAG_")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/tags", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200)
                .body("tags", hasItem(TAG));

        verify(paimonAdapter).listTags(DATABASE, TABLE, 5, "tt", "TAG_");
    }

    @Test
    void createTagParsesBody() {
        var request = new CreateTagRequest(TAG, null, null);

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/tags", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateTagRequest.class);
        verify(paimonAdapter).createTag(eq(DATABASE), eq(TABLE), captor.capture());
        assertEquals(TAG, captor.getValue().tagName());
    }

    @Test
    void getTagParsesPath() throws Exception {
        when(paimonAdapter.getTag(DATABASE, TABLE, TAG))
                .thenReturn(RESTApi.fromJson("{\"tagName\":\"TAG_0\"}", GetTagResponse.class));

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/tags/{tag}", CATALOG, DATABASE, TABLE, TAG)
                .then()
                .statusCode(200);

        verify(paimonAdapter).getTag(DATABASE, TABLE, TAG);
    }

    @Test
    void deleteTagParsesPath() {
        givenJson()
                .when()
                .delete("/paimon/v1/{catalog}/databases/{database}/tables/{table}/tags/{tag}", CATALOG, DATABASE, TABLE, TAG)
                .then()
                .statusCode(200);

        verify(paimonAdapter).deleteTag(DATABASE, TABLE, TAG);
    }
}
