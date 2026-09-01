package kasanari.server.http.paimon;

import io.quarkus.test.junit.QuarkusTest;
import kasanari.server.http.CatalogHttpTestSupport;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.rest.RESTApi;
import org.apache.paimon.rest.requests.AlterTableRequest;
import org.apache.paimon.rest.requests.AuthTableQueryRequest;
import org.apache.paimon.rest.requests.CommitTableRequest;
import org.apache.paimon.rest.requests.CreateTableRequest;
import org.apache.paimon.rest.requests.RegisterTableRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;
import org.apache.paimon.rest.requests.ResetConsumerRequest;
import org.apache.paimon.rest.requests.RollbackSchemaRequest;
import org.apache.paimon.rest.requests.RollbackTableRequest;
import org.apache.paimon.rest.responses.AuthTableQueryResponse;
import org.apache.paimon.rest.responses.CommitTableResponse;
import org.apache.paimon.rest.responses.GetTableResponse;
import org.apache.paimon.rest.responses.GetTableSnapshotResponse;
import org.apache.paimon.rest.responses.GetTableTokenResponse;
import org.apache.paimon.rest.responses.GetVersionSnapshotResponse;
import org.apache.paimon.rest.responses.ListConsumersResponse;
import org.apache.paimon.rest.responses.ListSnapshotsResponse;
import org.apache.paimon.rest.responses.ListTableDetailsResponse;
import org.apache.paimon.rest.responses.ListTablesGloballyResponse;
import org.apache.paimon.rest.responses.ListTablesResponse;
import org.apache.paimon.table.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PaimonTableHttpTest extends CatalogHttpTestSupport {
    @Test
    void listTablesParsesQueryParams() {
        when(paimonAdapter.listTables(DATABASE, 10, "tok", "T_%"))
                .thenReturn(new ListTablesResponse(List.of(TABLE), "next"));

        givenJson()
                .queryParam("maxResults", 10)
                .queryParam("pageToken", "tok")
                .queryParam("tableNamePattern", "T_%")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables", CATALOG, DATABASE)
                .then()
                .statusCode(200)
                .body("tables", hasItem(TABLE));

        verify(paimonAdapter).listTables(DATABASE, 10, "tok", "T_%");
    }

    @Test
    void createTableParsesBody() {
        var request = new CreateTableRequest(PaimonRequestBodies.tableId(DATABASE, TABLE), PaimonRequestBodies.tableSchema());

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateTableRequest.class);
        verify(paimonAdapter).createTable(eq(DATABASE), captor.capture());
        assertEquals(TABLE, captor.getValue().getIdentifier().getTableName());
        assertEquals(DATABASE, captor.getValue().getIdentifier().getDatabaseName());
    }

    @Test
    void listTableDetailsParsesFilters() {
        when(paimonAdapter.listTableDetails(DATABASE, 5, "p", "T_%", "table"))
                .thenReturn(new ListTableDetailsResponse(List.of(), null));

        givenJson()
                .queryParam("maxResults", 5)
                .queryParam("pageToken", "p")
                .queryParam("tableNamePattern", "T_%")
                .queryParam("tableType", "table")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/table-details", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listTableDetails(DATABASE, 5, "p", "T_%", "table");
    }

    @Test
    void listTablesGloballyParsesQueryParams() {
        when(paimonAdapter.listTablesGlobally("DB_%", "T_%", 8, "g"))
                .thenReturn(new ListTablesGloballyResponse(List.of(PaimonRequestBodies.tableId(DATABASE, TABLE)), null));

        givenJson()
                .queryParam("databaseNamePattern", "DB_%")
                .queryParam("tableNamePattern", "T_%")
                .queryParam("maxResults", 8)
                .queryParam("pageToken", "g")
                .when()
                .get("/paimon/v1/{catalog}/tables", CATALOG)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listTablesGlobally("DB_%", "T_%", 8, "g");
    }

    @Test
    void getTableByIdParsesPath() throws Exception {
        var response = RESTApi.fromJson("{\"id\":\"tid-1\",\"name\":\"T_0\"}", GetTableResponse.class);
        when(paimonAdapter.getTableById("tid-1")).thenReturn(response);

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/tables/id/{tableId}", CATALOG, "tid-1")
                .then()
                .statusCode(200);

        verify(paimonAdapter).getTableById("tid-1");
    }

    @Test
    void getTableParsesPath() throws Exception {
        var response = RESTApi.fromJson("{\"id\":\"1\",\"name\":\"T_0\"}", GetTableResponse.class);
        when(paimonAdapter.getTable(DATABASE, TABLE)).thenReturn(response);

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200)
                .body("name", equalTo(TABLE));

        verify(paimonAdapter).getTable(DATABASE, TABLE);
    }

    @Test
    void alterTableParsesBody() {
        var request = new AlterTableRequest(Collections.emptyList());

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).alterTable(eq(DATABASE), eq(TABLE), org.mockito.ArgumentMatchers.any(AlterTableRequest.class));
    }

    @Test
    void dropTableParsesPath() {
        givenJson()
                .when()
                .delete("/paimon/v1/{catalog}/databases/{database}/tables/{table}", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).dropTable(DATABASE, TABLE);
    }

    @Test
    void renameTableParsesBody() {
        var request = new RenameTableRequest(
                Identifier.create(DATABASE, TABLE),
                Identifier.create(DATABASE, "T_1")
        );

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/tables/rename", CATALOG)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RenameTableRequest.class);
        verify(paimonAdapter).renameTable(captor.capture());
        assertEquals(TABLE, captor.getValue().getSource().getTableName());
        assertEquals("T_1", captor.getValue().getDestination().getTableName());
    }

    @Test
    void registerTableParsesBody() {
        var request = new RegisterTableRequest(Identifier.create(DATABASE, TABLE), "/tmp/DB_0/T_0");

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/register", CATALOG, DATABASE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RegisterTableRequest.class);
        verify(paimonAdapter).registerTable(eq(DATABASE), captor.capture());
        assertEquals("/tmp/DB_0/T_0", captor.getValue().getPath());
    }

    @Test
    void commitTableParsesBody() {
        var request = new CommitTableRequest(null, null, Collections.emptyList());
        when(paimonAdapter.commitTable(eq(DATABASE), eq(TABLE), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new CommitTableResponse(true));

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/commit", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).commitTable(eq(DATABASE), eq(TABLE), org.mockito.ArgumentMatchers.any(CommitTableRequest.class));
    }

    @Test
    void rollbackTableParsesBody() {
        var request = new RollbackTableRequest(Instant.snapshot(42L), null);

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/rollback", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RollbackTableRequest.class);
        verify(paimonAdapter).rollbackTable(eq(DATABASE), eq(TABLE), captor.capture());
        var instant = (org.apache.paimon.table.Instant.SnapshotInstant) captor.getValue().getInstant();
        assertEquals(42L, instant.getSnapshotId());
    }

    @Test
    void rollbackSchemaParsesBody() {
        var request = new RollbackSchemaRequest(0L);

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/rollback-schema", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RollbackSchemaRequest.class);
        verify(paimonAdapter).rollbackSchema(eq(DATABASE), eq(TABLE), captor.capture());
        assertEquals(0L, captor.getValue().getSchemaId());
    }

    @Test
    void getTableTokenParsesPath() {
        when(paimonAdapter.getTableToken(DATABASE, TABLE))
                .thenReturn(new GetTableTokenResponse(Map.of("access-key", "abc"), 1L));

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/token", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).getTableToken(DATABASE, TABLE);
    }

    @Test
    void authTableQueryParsesBody() throws Exception {
        var request = new AuthTableQueryRequest(List.of("id"));
        when(paimonAdapter.authTableQuery(eq(DATABASE), eq(TABLE), org.mockito.ArgumentMatchers.any()))
                .thenReturn(RESTApi.fromJson("{\"filter\":[]}", AuthTableQueryResponse.class));

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/auth", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(AuthTableQueryRequest.class);
        verify(paimonAdapter).authTableQuery(eq(DATABASE), eq(TABLE), captor.capture());
        assertEquals(List.of("id"), captor.getValue().select());
    }

    @Test
    void getTableSnapshotParsesPath() throws Exception {
        when(paimonAdapter.getTableSnapshot(DATABASE, TABLE))
                .thenReturn(RESTApi.fromJson("{}", GetTableSnapshotResponse.class));

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/snapshot", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).getTableSnapshot(DATABASE, TABLE);
    }

    @Test
    void getVersionSnapshotParsesVersionPath() throws Exception {
        when(paimonAdapter.getVersionSnapshot(DATABASE, TABLE, "LATEST"))
                .thenReturn(RESTApi.fromJson("{}", GetVersionSnapshotResponse.class));

        givenJson()
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/snapshots/{version}", CATALOG, DATABASE, TABLE, "LATEST")
                .then()
                .statusCode(200);

        verify(paimonAdapter).getVersionSnapshot(DATABASE, TABLE, "LATEST");
    }

    @Test
    void listSnapshotsParsesPagination() {
        when(paimonAdapter.listSnapshots(DATABASE, TABLE, 4, "s"))
                .thenReturn(new ListSnapshotsResponse(List.of(), null));

        givenJson()
                .queryParam("maxResults", 4)
                .queryParam("pageToken", "s")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/snapshots", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listSnapshots(DATABASE, TABLE, 4, "s");
    }

    @Test
    void listConsumersParsesPagination() {
        when(paimonAdapter.listConsumers(DATABASE, TABLE, 2, "c"))
                .thenReturn(new ListConsumersResponse(List.of(), null));

        givenJson()
                .queryParam("maxResults", 2)
                .queryParam("pageToken", "c")
                .when()
                .get("/paimon/v1/{catalog}/databases/{database}/tables/{table}/consumers", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        verify(paimonAdapter).listConsumers(DATABASE, TABLE, 2, "c");
    }

    @Test
    void resetConsumerParsesBody() {
        var request = new ResetConsumerRequest("consumer-1", 9L);

        givenJson()
                .body(paimonJson(request))
                .when()
                .post("/paimon/v1/{catalog}/databases/{database}/tables/{table}/consumers/reset", CATALOG, DATABASE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(ResetConsumerRequest.class);
        verify(paimonAdapter).resetConsumer(eq(DATABASE), eq(TABLE), captor.capture());
        assertEquals("consumer-1", captor.getValue().consumerId());
        assertEquals(9L, captor.getValue().nextSnapshotId());
    }
}
