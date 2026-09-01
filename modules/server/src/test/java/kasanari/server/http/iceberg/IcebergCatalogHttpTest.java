package kasanari.server.http.iceberg;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.NotFoundException;
import kasanari.server.http.CatalogHttpTestSupport;
import org.apache.iceberg.MetadataUpdate;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SchemaParser;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.metrics.CommitMetrics;
import org.apache.iceberg.metrics.CommitMetricsResult;
import org.apache.iceberg.metrics.CommitReport;
import org.apache.iceberg.metrics.ImmutableCommitReport;
import org.apache.iceberg.rest.requests.CommitTransactionRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.ImmutableCreateViewRequest;
import org.apache.iceberg.rest.requests.ImmutableRegisterTableRequest;
import org.apache.iceberg.rest.requests.RenameTableRequest;
import org.apache.iceberg.rest.requests.ReportMetricsRequest;
import org.apache.iceberg.rest.requests.UpdateNamespacePropertiesRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.GetNamespaceResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.UpdateNamespacePropertiesResponse;
import org.apache.iceberg.view.ImmutableSQLViewRepresentation;
import org.apache.iceberg.view.ImmutableViewVersion;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class IcebergCatalogHttpTest extends CatalogHttpTestSupport {
    private static final Schema SCHEMA = SchemaParser.fromJson("""
            {"type":"struct","fields":[{"id":1,"name":"id","type":"long","required":true}]}
            """);
    private static final String ACCESS_DELEGATION = "vended-credentials";

    @Test
    void getConfigParsesWarehouseQueryParam() {
        givenJson()
                .queryParam("warehouse", CATALOG)
                .when()
                .get("/iceberg/v1/config")
                .then()
                .statusCode(200)
                .body("overrides.prefix", equalTo(CATALOG))
                .body("endpoints", notNullValue());
    }

    @Test
    void createNamespaceParsesJsonArrayAndProperties() {
        var expected = Namespace.of("NS_0", "NS_1");
        when(icebergAdapter.createNamespace(eq(expected), eq(Map.of("owner", "test"))))
                .thenReturn(CreateNamespaceResponse.builder()
                        .withNamespace(expected)
                        .setProperties(new HashMap<>(Map.of("owner", "test")))
                        .build());

        givenJson()
                .body("""
                        {"namespace":["NS_0","NS_1"],"properties":{"owner":"test"}}
                        """)
                .when()
                .post("/iceberg/v1/{catalog}/namespaces", CATALOG)
                .then()
                .statusCode(200)
                .body("namespace", equalTo(List.of("NS_0", "NS_1")));

        verify(icebergAdapter).createNamespace(eq(expected), eq(Map.of("owner", "test")));
    }

    @Test
    void createNamespaceRejectsMissingNamespace() {
        givenJson()
                .body("""
                        {"properties":{"owner":"test"}}
                        """)
                .when()
                .post("/iceberg/v1/{catalog}/namespaces", CATALOG)
                .then()
                .statusCode(400)
                .body("error.type", equalTo("IllegalArgumentException"));
    }

    @Test
    void listNamespacesParsesParentPageTokenAndPageSize() {
        when(icebergAdapter.listNamespaces("tok", 10, MULTIPART_NAMESPACE))
                .thenReturn(ListNamespacesResponse.builder().add(Namespace.of("NS_0", "NS_1", "NS_2")).build());

        givenJson()
                .queryParam("parent", MULTIPART_NAMESPACE)
                .queryParam("pageToken", "tok")
                .queryParam("pageSize", 10)
                .when()
                .get("/iceberg/v1/{catalog}/namespaces", CATALOG)
                .then()
                .statusCode(200)
                .body("namespaces", hasItem(List.of("NS_0", "NS_1", "NS_2")));

        verify(icebergAdapter).listNamespaces("tok", 10, MULTIPART_NAMESPACE);
    }

    @Test
    void loadNamespaceMetadataDecodesMultipartPath() {
        var ns = Namespace.of("NS_0", "NS_1");
        when(icebergAdapter.loadNamespaceMetadata(ns))
                .thenReturn(GetNamespaceResponse.builder()
                        .withNamespace(ns)
                        .setProperties(new HashMap<>(Map.of("k", "v")))
                        .build());

        givenJson()
                .when()
                .get("/iceberg/v1/{catalog}/namespaces/{namespace}", CATALOG, MULTIPART_NAMESPACE)
                .then()
                .statusCode(200)
                .body("namespace", equalTo(List.of("NS_0", "NS_1")));

        verify(icebergAdapter).loadNamespaceMetadata(ns);
    }

    @Test
    void namespaceExistsUsesHeadAndSingleLevelPath() {
        when(icebergAdapter.namespaceExists(Namespace.of(NAMESPACE))).thenReturn(true);

        givenJson()
                .when()
                .head("/iceberg/v1/{catalog}/namespaces/{namespace}", CATALOG, NAMESPACE)
                .then()
                .statusCode(204);

        verify(icebergAdapter).namespaceExists(Namespace.of(NAMESPACE));
    }

    @Test
    void dropNamespaceDecodesPath() {
        givenJson()
                .when()
                .delete("/iceberg/v1/{catalog}/namespaces/{namespace}", CATALOG, MULTIPART_NAMESPACE)
                .then()
                .statusCode(204);

        verify(icebergAdapter).dropNamespace(Namespace.of("NS_0", "NS_1"));
    }

    @Test
    void updateNamespacePropertiesParsesBody() {
        var ns = Namespace.of(NAMESPACE);
        when(icebergAdapter.updateNamespace(eq(ns), eq(Map.of("k", "v")), eq(Set.of("old"))))
                .thenReturn(UpdateNamespacePropertiesResponse.builder().addUpdated("k").addRemoved("old").build());

        var rq = UpdateNamespacePropertiesRequest.builder().update("k", "v").remove("old").build();
        givenJson()
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/properties", CATALOG, NAMESPACE)
                .then()
                .statusCode(200);

        verify(icebergAdapter).updateNamespace(eq(ns), eq(Map.of("k", "v")), eq(Set.of("old")));
    }

    @Test
    void listTablesParsesPaginationAndPath() {
        var ns = Namespace.of("NS_0", "NS_1");
        when(icebergAdapter.listTables(ns, "p", 5))
                .thenReturn(ListTablesResponse.builder().add(TableIdentifier.of(ns, TABLE)).build());

        givenJson()
                .queryParam("pageToken", "p")
                .queryParam("pageSize", 5)
                .when()
                .get("/iceberg/v1/{catalog}/namespaces/{namespace}/tables", CATALOG, MULTIPART_NAMESPACE)
                .then()
                .statusCode(200);

        verify(icebergAdapter).listTables(ns, "p", 5);
    }

    @Test
    void createTableParsesBodyAndIgnoresAccessDelegationHeader() {
        var ns = Namespace.of(NAMESPACE);
        var rq = CreateTableRequest.builder()
                .withName(TABLE)
                .withLocation("s3://warehouse/T_0")
                .withSchema(SCHEMA)
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .withWriteOrder(SortOrder.unsorted())
                .build();

        givenJson()
                .header("X-Iceberg-Access-Delegation", ACCESS_DELEGATION)
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/tables", CATALOG, NAMESPACE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateTableRequest.class);
        verify(icebergAdapter).createTable(eq(ns), captor.capture());
        assertEquals(TABLE, captor.getValue().name());
        assertEquals("s3://warehouse/T_0", captor.getValue().location());
        assertEquals(SCHEMA.asStruct(), captor.getValue().schema().asStruct());
    }

    @Test
    void loadTableParsesPathAndBindsIgnoredQueryParams() {
        var identifier = TableIdentifier.of(Namespace.of(NAMESPACE), TABLE);

        givenJson()
                .header("X-Iceberg-Access-Delegation", ACCESS_DELEGATION)
                .header("If-None-Match", "etag")
                .queryParam("snapshots", "all")
                .queryParam("referenced-by", "engine")
                .when()
                .get("/iceberg/v1/{catalog}/namespaces/{namespace}/tables/{table}", CATALOG, NAMESPACE, TABLE)
                .then()
                .statusCode(200);

        verify(icebergAdapter).loadTable(identifier);
    }

    @Test
    void updateTableParsesCommitBody() {
        var identifier = TableIdentifier.of(Namespace.of(NAMESPACE), TABLE);
        var rq = UpdateTableRequest.create(
                identifier,
                List.of(),
                List.of(new MetadataUpdate.SetProperties(Map.of("k", "v")))
        );

        givenJson()
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/tables/{table}", CATALOG, NAMESPACE, TABLE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(UpdateTableRequest.class);
        verify(icebergAdapter).updateTable(eq(identifier), captor.capture());
        assertEquals(1, captor.getValue().updates().size());
    }

    @Test
    void dropTableParsesPurgeRequested() {
        givenJson()
                .queryParam("purgeRequested", true)
                .when()
                .delete("/iceberg/v1/{catalog}/namespaces/{namespace}/tables/{table}", CATALOG, NAMESPACE, TABLE)
                .then()
                .statusCode(204);

        verify(icebergAdapter).dropTable(TableIdentifier.of(Namespace.of(NAMESPACE), TABLE), true);
    }

    @Test
    void tableExistsUsesHead() {
        when(icebergAdapter.tableExists(TableIdentifier.of(Namespace.of(NAMESPACE), TABLE))).thenReturn(true);

        givenJson()
                .when()
                .head("/iceberg/v1/{catalog}/namespaces/{namespace}/tables/{table}", CATALOG, NAMESPACE, TABLE)
                .then()
                .statusCode(204);

        verify(icebergAdapter).tableExists(TableIdentifier.of(Namespace.of(NAMESPACE), TABLE));
    }

    @Test
    void registerTableParsesBody() {
        var identifier = TableIdentifier.of(Namespace.of(NAMESPACE), TABLE);
        var rq = ImmutableRegisterTableRequest.builder()
                .name(TABLE)
                .metadataLocation("s3://warehouse/metadata.json")
                .build();

        givenJson()
                .header("X-Iceberg-Access-Delegation", ACCESS_DELEGATION)
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/register", CATALOG, NAMESPACE)
                .then()
                .statusCode(200);

        verify(icebergAdapter).registerTable(identifier, "s3://warehouse/metadata.json");
    }

    @Test
    void renameTableParsesSourceAndDestination() {
        var source = TableIdentifier.of(Namespace.of("NS_0"), "T_0");
        var destination = TableIdentifier.of(Namespace.of("NS_1"), "T_1");
        var rq = RenameTableRequest.builder().withSource(source).withDestination(destination).build();

        givenJson()
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/tables/rename", CATALOG)
                .then()
                .statusCode(204);

        verify(icebergAdapter).renameTable(source, destination);
    }

    @Test
    void commitTransactionParsesTableChanges() {
        var identifier = TableIdentifier.of(Namespace.of(NAMESPACE), TABLE);
        var change = UpdateTableRequest.create(
                identifier,
                List.of(),
                List.of(new MetadataUpdate.SetProperties(Map.of("txn", "1")))
        );
        var rq = new CommitTransactionRequest(List.of(change));

        givenJson()
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/transactions/commit", CATALOG)
                .then()
                .statusCode(204);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UpdateTableRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(icebergAdapter).commitTransaction(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(identifier, captor.getValue().getFirst().identifier());
    }

    @Test
    void listViewsParsesPagination() {
        var ns = Namespace.of(NAMESPACE);
        when(icebergAdapter.listViews(ns, "vtok", 3))
                .thenReturn(ListTablesResponse.builder().add(TableIdentifier.of(ns, VIEW)).build());

        givenJson()
                .queryParam("pageToken", "vtok")
                .queryParam("pageSize", 3)
                .when()
                .get("/iceberg/v1/{catalog}/namespaces/{namespace}/views", CATALOG, NAMESPACE)
                .then()
                .statusCode(200);

        verify(icebergAdapter).listViews(ns, "vtok", 3);
    }

    @Test
    void createViewParsesBody() {
        var ns = Namespace.of(NAMESPACE);
        var rq = defaultCreateViewRequest(ns, VIEW);

        givenJson()
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/views", CATALOG, NAMESPACE)
                .then()
                .statusCode(200);

        var captor = ArgumentCaptor.forClass(CreateViewRequest.class);
        verify(icebergAdapter).createView(eq(ns), captor.capture());
        assertEquals(VIEW, captor.getValue().name());
        assertEquals("s3://warehouse/V_0", captor.getValue().location());
    }

    @Test
    void loadViewParsesPath() {
        givenJson()
                .queryParam("referenced-by", "engine")
                .when()
                .get("/iceberg/v1/{catalog}/namespaces/{namespace}/views/{view}", CATALOG, NAMESPACE, VIEW)
                .then()
                .statusCode(200);

        verify(icebergAdapter).loadView(TableIdentifier.of(Namespace.of(NAMESPACE), VIEW));
    }

    @Test
    void replaceViewParsesBody() {
        var identifier = TableIdentifier.of(Namespace.of(NAMESPACE), VIEW);
        var rq = UpdateTableRequest.create(
                identifier,
                List.of(),
                List.of(new MetadataUpdate.SetLocation("s3://warehouse/V_0-new"))
        );

        givenJson()
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/views/{view}", CATALOG, NAMESPACE, VIEW)
                .then()
                .statusCode(200);

        verify(icebergAdapter).replaceView(eq(identifier), any(UpdateTableRequest.class));
    }

    @Test
    void dropViewParsesPath() {
        givenJson()
                .when()
                .delete("/iceberg/v1/{catalog}/namespaces/{namespace}/views/{view}", CATALOG, NAMESPACE, VIEW)
                .then()
                .statusCode(204);

        verify(icebergAdapter).dropView(TableIdentifier.of(Namespace.of(NAMESPACE), VIEW));
    }

    @Test
    void viewExistsUsesHead() {
        when(icebergAdapter.viewExists(TableIdentifier.of(Namespace.of(NAMESPACE), VIEW))).thenReturn(true);

        givenJson()
                .when()
                .head("/iceberg/v1/{catalog}/namespaces/{namespace}/views/{view}", CATALOG, NAMESPACE, VIEW)
                .then()
                .statusCode(204);

        verify(icebergAdapter).viewExists(TableIdentifier.of(Namespace.of(NAMESPACE), VIEW));
    }

    @Test
    void renameViewParsesSourceAndDestination() {
        var source = TableIdentifier.of(Namespace.of(NAMESPACE), VIEW);
        var destination = TableIdentifier.of(Namespace.of(NAMESPACE), "V_1");
        var rq = RenameTableRequest.builder().withSource(source).withDestination(destination).build();

        givenJson()
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/views/rename", CATALOG)
                .then()
                .statusCode(204);

        verify(icebergAdapter).renameView(source, destination);
    }

    @Test
    void registerViewParsesBody() {
        givenJson()
                .body("""
                        {"name":"V_0","metadata-location":"s3://warehouse/view-metadata.json"}
                        """)
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/register-view", CATALOG, NAMESPACE)
                .then()
                .statusCode(200);

        verify(icebergAdapter).registerView(
                TableIdentifier.of(Namespace.of(NAMESPACE), VIEW),
                "s3://warehouse/view-metadata.json"
        );
    }

    @Test
    void reportMetricsParsesBody() {
        CommitReport report = ImmutableCommitReport.builder()
                .tableName("NS_0.T_0")
                .snapshotId(1L)
                .sequenceNumber(1L)
                .operation("append")
                .commitMetrics(CommitMetricsResult.from(CommitMetrics.noop(), Map.of()))
                .build();
        var rq = ReportMetricsRequest.of(report);

        givenJson()
                .body(icebergJson(rq))
                .when()
                .post("/iceberg/v1/{catalog}/namespaces/{namespace}/tables/{table}/metrics", CATALOG, NAMESPACE, TABLE)
                .then()
                .statusCode(204);

        var captor = ArgumentCaptor.forClass(ReportMetricsRequest.class);
        verify(icebergAdapter).reportMetrics(eq(NAMESPACE), eq(TABLE), captor.capture());
        assertEquals(ReportMetricsRequest.ReportType.COMMIT_REPORT, captor.getValue().reportType());
    }

    @Test
    void unknownCatalogMapsToIcebergNotFoundError() {
        when(icebergCatalogRouter.getOrThrow("missing")).thenThrow(new NotFoundException("Iceberg catalog wasn't found"));

        givenJson()
                .when()
                .get("/iceberg/v1/missing/namespaces")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error.type", equalTo("NotFoundException"))
                .body("error.code", equalTo(404));
    }

    @Test
    void dottedNamespaceIsASingleLevelNotMultipart() {
        when(icebergAdapter.namespaceExists(Namespace.of("NS_0.NS_1"))).thenReturn(true);

        givenJson()
                .when()
                .head("/iceberg/v1/{catalog}/namespaces/{namespace}", CATALOG, "NS_0.NS_1")
                .then()
                .statusCode(204);

        verify(icebergAdapter).namespaceExists(Namespace.of("NS_0.NS_1"));
    }

    private static CreateViewRequest defaultCreateViewRequest(Namespace namespace, String name) {
        return ImmutableCreateViewRequest.builder()
                .name(name)
                .location("s3://warehouse/V_0")
                .schema(SCHEMA)
                .viewVersion(
                        ImmutableViewVersion.builder()
                                .versionId(1)
                                .timestampMillis(1)
                                .schemaId(0)
                                .putAllSummary(Map.of())
                                .addAllRepresentations(List.of(
                                        ImmutableSQLViewRepresentation.builder()
                                                .dialect("spark")
                                                .sql("select 1")
                                                .build()
                                ))
                                .defaultNamespace(namespace)
                                .build()
                )
                .build();
    }
}
