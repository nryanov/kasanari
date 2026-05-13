package kasanari.catalog.lance;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lance.namespace.model.AlterTransactionAction;
import org.lance.namespace.model.AlterTransactionRequest;
import org.lance.namespace.model.AlterTransactionSetProperty;
import org.lance.namespace.model.BatchCommitTablesRequest;
import org.lance.namespace.model.CommitTableOperation;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateTableVersionRequest;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DescribeTransactionRequest;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListTableVersionsRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.TableVersion;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KasanariLanceCatalogTest {
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private LanceCatalogAdapter adapter;

    @BeforeEach
    void setup() {
        postgres.start();
        var properties = Map.of(
                KasanariDataSourceConfiguration.URI, postgres.jdbcUrl(),
                KasanariDataSourceConfiguration.USER, postgres.username(),
                KasanariDataSourceConfiguration.PASSWORD, postgres.password()
        );
        adapter = new KasanariLanceCatalogFactory().create("kasanari", properties);
    }

    @AfterEach
    void teardown() throws Exception {
        if (adapter != null) {
            adapter.close();
        }
        postgres.stop();
    }

    @Test
    void supportsNamespaceTableVersionAndTransactionMetadataFlow() {
        adapter.createNamespace(new CreateNamespaceRequest()
                .id(List.of("analytics"))
                .properties(Map.of("owner", "kasanari")));

        var namespaces = adapter.listNamespaces(new ListNamespacesRequest());
        assertTrue(namespaces.getNamespaces().contains("analytics"));

        adapter.registerTable(new RegisterTableRequest()
                .id(List.of("analytics", "events"))
                .location("s3://warehouse/analytics/events")
                .properties(Map.of("format", "lance")));

        var table = adapter.describeTable(new DescribeTableRequest().id(List.of("analytics", "events")));
        assertEquals("events", table.getTable());
        assertFalse(Boolean.TRUE.equals(table.getIsOnlyDeclared()));

        adapter.createTableVersion(new CreateTableVersionRequest()
                .id(List.of("analytics", "events"))
                .version(1L)
                .manifestPath("s3://warehouse/analytics/events/_versions/1.manifest")
                .metadata(Map.of("source", "test")));

        var versions = adapter.listTableVersions(new ListTableVersionsRequest().id(List.of("analytics", "events")));
        assertEquals(1, versions.getVersions().size());
        var version = (TableVersion) versions.getVersions().get(0);
        assertEquals(1L, version.getVersion());

        var commit = adapter.batchCommitTables(new BatchCommitTablesRequest()
                .operations(List.of(new CommitTableOperation().createTableVersion(
                        new CreateTableVersionRequest()
                                .id(List.of("analytics", "events"))
                                .version(2L)
                                .manifestPath("s3://warehouse/analytics/events/_versions/2.manifest")
                ))));
        assertEquals(1, commit.getResults().size());

        var transactionId = commit.getTransactionId();
        var alterRequest = new AlterTransactionRequest()
                .id(List.of(transactionId))
                .actions(List.of(new AlterTransactionAction().setPropertyAction(
                        new AlterTransactionSetProperty().key("note").value("updated"))));
        var altered = adapter.alterTransaction(alterRequest);
        assertTrue(altered.getProperties().containsKey("note"));

        var described = adapter.describeTransaction(new DescribeTransactionRequest().id(List.of(transactionId)));
        assertTrue(described.getProperties().containsKey("note"));
    }
}
