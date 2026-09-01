package kasanari.server.http.lance;

import io.quarkus.test.junit.QuarkusTest;
import kasanari.server.http.CatalogHttpTestSupport;
import org.junit.jupiter.api.Test;
import org.lance.namespace.model.AlterTableAddColumnsRequest;
import org.lance.namespace.model.AlterTableAddColumnsResponse;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableAlterColumnsResponse;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsResponse;
import org.lance.namespace.model.CreateTableRequest;
import org.lance.namespace.model.CreateTableResponse;
import org.lance.namespace.model.DeclareTableRequest;
import org.lance.namespace.model.DeclareTableResponse;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DeregisterTableResponse;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DescribeTableResponse;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.DropTableResponse;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RegisterTableResponse;
import org.lance.namespace.model.RenameTableRequest;
import org.lance.namespace.model.RenameTableResponse;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.RestoreTableResponse;
import org.lance.namespace.model.TableExistsRequest;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class LanceTableHttpTest extends CatalogHttpTestSupport {
    @Test
    void createTableParsesBinaryBodyAndJsonQueryMaps() {
        when(lanceAdapter.createTable(any(), any()))
                .thenReturn(new CreateTableResponse().location("file:/tmp/T_0"));

        var arrow = new byte[] {0x01, 0x02, 0x03};
        given()
                .contentType("application/vnd.apache.arrow.stream")
                .queryParam("mode", "create")
                .queryParam("properties", "{\"user\":\"alice\"}")
                .queryParam("storage_options", "{\"timeout\":\"30s\"}")
                .body(arrow)
                .when()
                .post("/lance/v1/table/{id}/create", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var requestCaptor = ArgumentCaptor.forClass(CreateTableRequest.class);
        var bodyCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(lanceAdapter).createTable(requestCaptor.capture(), bodyCaptor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), requestCaptor.getValue().getId());
        assertEquals("create", requestCaptor.getValue().getMode());
        assertEquals("alice", requestCaptor.getValue().getProperties().get("user"));
        assertEquals("30s", requestCaptor.getValue().getStorageOptions().get("timeout"));
        assertArrayEquals(arrow, bodyCaptor.getValue());
    }

    @Test
    void createTableParsesCustomDelimiter() {
        when(lanceAdapter.createTable(any(), any())).thenReturn(new CreateTableResponse());

        given()
                .contentType("application/vnd.apache.arrow.stream")
                .queryParam("delimiter", LANCE_CUSTOM_DELIMITER)
                .body(new byte[0])
                .when()
                .post("/lance/v1/table/{id}/create", LANCE_TABLE_ID_CUSTOM)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateTableRequest.class);
        verify(lanceAdapter).createTable(captor.capture(), any());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
    }

    @Test
    void registerTableParsesLocationAndOverwritesId() {
        when(lanceAdapter.registerTable(any())).thenReturn(new RegisterTableResponse());

        givenJson()
                .body("""
                        {"location":"file:/tmp/T_0","mode":"create"}
                        """)
                .when()
                .post("/lance/v1/table/{id}/register", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RegisterTableRequest.class);
        verify(lanceAdapter).registerTable(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
        assertEquals("file:/tmp/T_0", captor.getValue().getLocation());
    }

    @Test
    void describeTableBindsIgnoredQueryParamsAndOverwritesId() {
        when(lanceAdapter.describeTable(any())).thenReturn(new DescribeTableResponse());

        givenJson()
                .queryParam("with_table_uri", true)
                .queryParam("load_detailed_metadata", true)
                .queryParam("check_declared", false)
                .body("{}")
                .when()
                .post("/lance/v1/table/{id}/describe", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(DescribeTableRequest.class);
        verify(lanceAdapter).describeTable(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
    }

    @Test
    void tableExistsOverwritesId() {
        doNothing().when(lanceAdapter).tableExists(any());

        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/table/{id}/exists", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(TableExistsRequest.class);
        verify(lanceAdapter).tableExists(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
    }

    @Test
    void dropTableBuildsRequestFromPath() {
        when(lanceAdapter.dropTable(any())).thenReturn(new DropTableResponse());

        givenJson()
                .when()
                .post("/lance/v1/table/{id}/drop", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(DropTableRequest.class);
        verify(lanceAdapter).dropTable(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
    }

    @Test
    void deregisterTableOverwritesId() {
        when(lanceAdapter.deregisterTable(any())).thenReturn(new DeregisterTableResponse());

        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/table/{id}/deregister", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(DeregisterTableRequest.class);
        verify(lanceAdapter).deregisterTable(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
    }

    @Test
    void restoreTableParsesVersion() {
        when(lanceAdapter.restoreTable(any())).thenReturn(new RestoreTableResponse());

        givenJson()
                .body("""
                        {"version":7}
                        """)
                .when()
                .post("/lance/v1/table/{id}/restore", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RestoreTableRequest.class);
        verify(lanceAdapter).restoreTable(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
        assertEquals(7L, captor.getValue().getVersion());
    }

    @Test
    void renameTableParsesNewName() {
        when(lanceAdapter.renameTable(any())).thenReturn(new RenameTableResponse());

        givenJson()
                .body("""
                        {"new_table_name":"T_1","new_namespace_id":["NS_1"]}
                        """)
                .when()
                .post("/lance/v1/table/{id}/rename", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(RenameTableRequest.class);
        verify(lanceAdapter).renameTable(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
        assertEquals("T_1", captor.getValue().getNewTableName());
        assertEquals(List.of("NS_1"), captor.getValue().getNewNamespaceId());
    }

    @Test
    void alterColumnsParsesBody() {
        when(lanceAdapter.alterTableAlterColumns(any())).thenReturn(new AlterTableAlterColumnsResponse());

        givenJson()
                .body("""
                        {"alterations":[]}
                        """)
                .when()
                .post("/lance/v1/table/{id}/alter_columns", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(AlterTableAlterColumnsRequest.class);
        verify(lanceAdapter).alterTableAlterColumns(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
        assertEquals(List.of(), captor.getValue().getAlterations());
    }

    @Test
    void dropColumnsParsesBody() {
        when(lanceAdapter.alterTableDropColumns(any())).thenReturn(new AlterTableDropColumnsResponse());

        givenJson()
                .body("""
                        {"columns":["id"]}
                        """)
                .when()
                .post("/lance/v1/table/{id}/drop_columns", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(AlterTableDropColumnsRequest.class);
        verify(lanceAdapter).alterTableDropColumns(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
        assertEquals(List.of("id"), captor.getValue().getColumns());
    }

    @Test
    void addColumnsParsesBody() {
        when(lanceAdapter.alterTableAddColumns(any())).thenReturn(new AlterTableAddColumnsResponse());

        givenJson()
                .body("""
                        {"new_columns":[]}
                        """)
                .when()
                .post("/lance/v1/table/{id}/add_columns", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(AlterTableAddColumnsRequest.class);
        verify(lanceAdapter).alterTableAddColumns(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
        assertEquals(List.of(), captor.getValue().getNewColumns());
    }

    @Test
    void declareTableParsesBody() {
        when(lanceAdapter.createEmptyTable(any())).thenReturn(new DeclareTableResponse());

        givenJson()
                .body("""
                        {"location":"file:/tmp/empty"}
                        """)
                .when()
                .post("/lance/v1/table/{id}/declare", LANCE_TABLE_ID)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(DeclareTableRequest.class);
        verify(lanceAdapter).createEmptyTable(captor.capture());
        assertEquals(List.of(NAMESPACE, TABLE), captor.getValue().getId());
        assertEquals("file:/tmp/empty", captor.getValue().getLocation());
    }

    @Test
    void invalidTableIdMapsToBadRequest() {
        givenJson()
                .body("{}")
                .when()
                .post("/lance/v1/table/{id}/exists", "C_0.only-two")
                .then()
                .statusCode(400);
    }
}
