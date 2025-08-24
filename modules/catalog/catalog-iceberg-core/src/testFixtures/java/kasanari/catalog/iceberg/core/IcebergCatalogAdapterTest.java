package kasanari.catalog.iceberg.core;

import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.ImmutableCreateViewRequest;
import org.apache.iceberg.view.ImmutableSQLViewRepresentation;
import org.apache.iceberg.view.ImmutableViewVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IcebergCatalogAdapterTest {
    protected IcebergCatalogAdapter catalog;

    @BeforeAll
    public final void setup() {
        catalog = setupCatalog();
    }

    abstract public IcebergCatalogAdapter setupCatalog();

    @AfterAll
    public void close() {

    }

    @BeforeEach
    public final void beforeEach() {
        reset();
    }

    public void reset() {}

    @Test
    public void returnEmptyNamespaceList() {
        var result = catalog.listNamespaces(String.valueOf(Integer.MAX_VALUE - 15), 10, null);

        assertTrue(result.namespaces().isEmpty());
    }

    @Test
    public void returnFalseIfNamespaceDoesNotExist() {
        var namespaceName = Namespace.of("ns1");
        var result = catalog.namespaceExists(namespaceName);

        assertFalse(result);
    }

    @Test
    public void successfullyCreateNamespace() {
        var namespace = Namespace.of("ns2");
        catalog.createNamespace(namespace);
        var result = catalog.namespaceExists(namespace);

        assertTrue(result);
    }

    @Test
    public void successfullyDeleteExistingNamespace() {
        var namespace = Namespace.of("ns3");
        catalog.createNamespace(namespace);
        var result = catalog.namespaceExists(namespace);

        assertTrue(result);

        catalog.dropNamespace(namespace);

        var resultAfterDeleting = catalog.namespaceExists(namespace);

        assertFalse(resultAfterDeleting);
    }

    @Test
    public void returnNonEmptyNamespaceList() {
        var namespace = Namespace.of("ns4");
        catalog.createNamespace(namespace);

        var result = catalog.listNamespaces(null, 10, null);

        assertFalse(result.namespaces().isEmpty());
    }

    @Test
    public void successfullyLoadNamespace() {
        var namespace = Namespace.of("ns5");
        catalog.createNamespace(namespace, Map.of("prop1", "value"));
        var loadedNamespace = catalog.loadNamespaceMetadata(namespace);

        var expectedProps = Map.of("prop1", "value");

        assertEquals(expectedProps, loadedNamespace.properties());
        assertEquals(namespace, loadedNamespace.namespace());
    }

    @Test
    public void successfullyUpdateNamespaceProperties() {
        var namespace = Namespace.of("ns6");
        var properties = Map.of(
                "property1", "value1",
                "property2", "value2",
                "property3", "value3"
        );
        catalog.createNamespace(namespace, properties);

        catalog.updateNamespace(namespace, Map.of("property4", "value4"), Set.of("property2"));

        var loadedNamespace = catalog.loadNamespaceMetadata(namespace);
        var expectedProperties = Map.of(
                "property1", "value1",
                "property3", "value3",
                "property4", "value4"
        );

        assertEquals(expectedProperties, loadedNamespace.properties());
    }

    @Test
    public void correctlyPaginateNamespaceListing() {
        var namespaceParent = Namespace.of("ns7");
        var namespace1 = Namespace.of("ns7", "1");
        var namespace2 = Namespace.of("ns7", "2");

        catalog.createNamespace(namespaceParent);
        catalog.createNamespace(namespace1);
        catalog.createNamespace(namespace2);

        var page1 = catalog.listNamespaces(null, 1, "ns7");

        assertEquals(List.of(namespace1), page1.namespaces());

        var page2 = catalog.listNamespaces(page1.nextPageToken(), 1, "ns7");
        assertEquals(List.of(namespace2), page2.namespaces());
    }

    @Test
    public void returnFalseIfViewDoesNotExist() {
        var namespace = Namespace.of("ns_view1");
        catalog.createNamespace(namespace);

        var view = TableIdentifier.of(namespace, "view");
        var result = catalog.viewExists(view);

        assertFalse(result);
    }

    @Test
    public void returnEmptyViewListing() {
        var namespace = Namespace.of("ns_view2");
        catalog.createNamespace(namespace);

        var result = catalog.listViews(namespace, null, 10);

        assertTrue(result.identifiers().isEmpty());
    }

    @Test
    public void successfullyCreateView() {
        var namespace = Namespace.of("ns_view3");
        catalog.createNamespace(namespace);

        var view = TableIdentifier.of(namespace, "view");
        var rq = ImmutableCreateViewRequest
                .builder()
                .name("view")
                .location("location")
                .schema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .viewVersion(
                        ImmutableViewVersion
                                .builder()
                                .versionId(1)
                                .timestampMillis(1)
                                .schemaId(1)
                                .putAllSummary(Map.of())
                                .addAllRepresentations(
                                        List.of(
                                                ImmutableSQLViewRepresentation
                                                        .builder()
                                                        .dialect("sql")
                                                        .sql("select * from table")
                                                        .build()
                                        )
                                )
                                .build()
                )
                .build();
        catalog.createView(namespace, rq);

        var result = catalog.viewExists(view);
        assertTrue(result);
    }


    /*
        public static IcebergView.CreateRequest defaultCreateViewRequest(
            IcebergNamespace.Name namespaceName,
            IcebergView.Name viewName
    ) {
        return new IcebergView.CreateRequest(
                new IcebergView.Metadata.Version(
                        Optional.empty(),
                        namespaceName
                ),
                Map.of()
        );
    }
     */





//
//    @Test
//    public void successfullyDropView() {
//        var namespaceName = new IcebergNamespace.Name("ns_view4");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var viewName = new IcebergView.Name("view");
//        var view = IcebergCatalogCommons.defaultCreateViewRequest(namespaceName, viewName);
//        catalog.createView(view);
//
//        assertTrue(catalog.viewExists(namespaceName, view.name()));
//
//        catalog.dropView(new IcebergView(namespaceName, view.name()));
//
//        var resultAfterDelete = catalog.viewExists(namespaceName, view.name());
//        assertFalse(resultAfterDelete);
//    }
//
//    @Test
//    public void returnNonEmptyListOfViews() {
//        var namespaceName = new IcebergNamespace.Name("ns_view5");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var viewName = new IcebergView.Name("view");
//        var view = IcebergCatalogCommons.defaultCreateViewRequest(namespaceName, viewName);
//        catalog.createView(view);
//
//        var filter = new IcebergView.Listing.Filter();
//        var result = catalog.listViews(namespaceName, filter);
//
//        var expectedView = new IcebergView(namespaceName, viewName);
//        assertEquals(List.of(expectedView), result.views());
//    }
//
//    @Test
//    public void successfullyRenameView() {
//        var namespaceName = new IcebergNamespace.Name("ns_view6");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var viewName = new IcebergView.Name("view");
//        var view = IcebergCatalogCommons.defaultCreateViewRequest(namespaceName, viewName);
//        catalog.createView(view);
//
//        var newViewName = new IcebergView.Name("renamed_view");
//
//        var oldView = new IcebergView(namespaceName, viewName);
//        var newView = new IcebergView(namespaceName, newViewName);
//
//        catalog.renameView(oldView, newView);
//
//        assertFalse(catalog.viewExists(namespaceName, oldView.name()));
//        assertTrue(catalog.viewExists(namespaceName, newView.name()));
//    }
//
//    @Test
//    public void successfullyLoadView() {
//        var namespaceName = new IcebergNamespace.Name("ns_view7");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var viewName = new IcebergView.Name("view");
//        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespaceName, viewName);
//        catalog.createView(rq);
//
//        var view = new IcebergView(namespaceName, viewName);
//        var result = catalog.loadView(view);
//
//        assertEquals(new IcebergValues.VersionId(1), result.currentVersionId());
//        assertEquals(new IcebergValues.Location("location"), result.location());
//        assertEquals(new IcebergValues.FormatVersion(1), result.formatVersion());
//
//        assertEquals(1, result.versions().size());
//        assertEquals(1, result.versionLog().size());
//
//        var resultVersion = result.versions().get(0);
//
//        assertEquals(1, resultVersion.representations().size());
//
//        var resultVersionRepresentation = resultVersion.representations().get(0);
//
//        assertEquals(new IcebergView.Metadata.Version.Representation(
//                new IcebergView.Metadata.Version.Representation.Type("sql"),
//                new IcebergView.Metadata.Version.Representation.Sql("select * from table"),
//                new IcebergView.Metadata.Version.Representation.Dialect("sql")
//        ), resultVersionRepresentation);
//
//        var resultVersionLog = result.versionLog().get(0);
//
//        assertEquals(new IcebergValues.VersionId(1), resultVersionLog.versionId());
//
//        assertEquals(1, result.schemas().size());
//    }
//
//    @Test
//    public void successfullyReplaceView() {
//        var namespaceName = new IcebergNamespace.Name("ns_view8");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var viewName = new IcebergView.Name("view");
//        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespaceName, viewName);
//        var metadata = catalog.createView(rq);
//
//        var view = new IcebergView(namespaceName, viewName);
//        var updateRq = new IcebergView.UpdateRequest(
//                List.of(new IcebergView.UpdateRequest.Requirement.AssertViewUUID(metadata.uuid())),
//                List.of(new IcebergView.UpdateRequest.Update.SetLocationUpdate(new IcebergValues.Location("newLocation")))
//        );
//
//        catalog.replaceView(view, updateRq);
//
//        var result = catalog.loadView(view);
//
//        assertEquals(new IcebergValues.Location("newLocation"), result.location());
//    }
//
//    // todo: add tests for each view#Requirement & view#Update
//    // todo: add tests for each table#Requirement & table#Update
//    // todo: add tests for each table#Transform
//
//    @Test
//    public void returnFalseIfTableDoesNotExist() {
//        var namespaceName = new IcebergNamespace.Name("ns_table1");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var result = catalog.tableExists(namespaceName, tableName);
//
//        assertFalse(result);
//    }
//
//    @Test
//    public void returnEmptyTableListing() {
//        var namespaceName = new IcebergNamespace.Name("ns_table2");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var filter = new IcebergTable.Listing.Filter();
//        var result = catalog.listTables(namespaceName, filter);
//
//        assertTrue(result.tables().isEmpty());
//    }
//
//    @Test
//    public void successfullyCreateUnpartitionedAndUnsortedTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table3");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                new IcebergValues.Location("location"),
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of()
//        );
//
//        catalog.createTable(rq);
//        var result = catalog.tableExists(namespaceName, tableName);
//        assertTrue(result);
//    }
//
//    @Test
//    public void successfullyCreatePartitionedTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table4");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                new IcebergValues.Location("location"),
//                new IcebergTable.PartitionSpecification.Partitioned(
//                        Optional.of(new IcebergTable.PartitionSpecification.Id(1)),
//                        List.of(
//                                new IcebergTable.PartitionSpecification.Partitioned.Field(
//                                        Optional.of(new IcebergValues.ColumnId(1)),
//                                        new IcebergValues.SourceId(1),
//                                        new IcebergTable.PartitionSpecification.Partitioned.Field.Name("id"),
//                                        new IcebergTable.Transform.Bucket(16)
//                                )
//                        )
//                ),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of()
//        );
//
//        catalog.createTable(rq);
//        var result = catalog.tableExists(namespaceName, tableName);
//        assertTrue(result);
//    }
//
//    @Test
//    public void successfullyCreateSortedTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table5");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                new IcebergValues.Location("location"),
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Sorted(
//                        new IcebergTable.SortSpecification.Id(1),
//                        List.of(
//                                new IcebergTable.SortSpecification.Sorted.Field(
//                                        new IcebergValues.SourceId(1),
//                                        new IcebergTable.Transform.Truncate(3),
//                                        IcebergTable.SortSpecification.Sorted.Direction.ASC,
//                                        IcebergTable.SortSpecification.Sorted.NullOrder.NULLS_FIRST
//                                )
//                        )
//                ),
//                Map.of()
//        );
//
//        catalog.createTable(rq);
//        var result = catalog.tableExists(namespaceName, tableName);
//        assertTrue(result);
//    }
//
//    @Test
//    public void successfullyDropTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table6");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                new IcebergValues.Location("location"),
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of()
//        );
//
//        catalog.createTable(rq);
//        assertTrue(catalog.tableExists(namespaceName, tableName));
//
//        var table = new IcebergTable(namespaceName, tableName);
//        catalog.dropTable(table, true);
//
//        var result = catalog.tableExists(namespaceName, tableName);
//        assertFalse(result);
//    }
//
//    @Test
//    public void successfullyRenameTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table7");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                new IcebergValues.Location("location"),
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of()
//        );
//
//        catalog.createTable(rq);
//        assertTrue(catalog.tableExists(namespaceName, tableName));
//
//        var newTableName = new IcebergTable.Name("newTable");
//        var oldTable = new IcebergTable(namespaceName, tableName);
//        var newTable = new IcebergTable(namespaceName, newTableName);
//        catalog.renameTable(oldTable, newTable);
//
//        assertFalse(catalog.tableExists(namespaceName, oldTable.name()));
//        assertTrue(catalog.tableExists(namespaceName, newTable.name()));
//    }
//
//    @Test
//    public void successfullyUpdateTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table8");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                new IcebergValues.Location("location"),
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of(
//                        "custom-property", "value"
//                )
//        );
//
//        var createdTableMetadata = catalog.createTable(rq);
//
//        var update = new IcebergTable.UpdateRequest(
//                List.of(
//                        new IcebergTable.UpdateRequest.Requirement.AssertTableUuid(createdTableMetadata.metadata().uuid())
//                ),
//                List.of(
//                        new IcebergTable.UpdateRequest.Update.SetPropertiesUpdate(
//                                Map.of("custom-property", "updated-value")
//                        )
//                )
//        );
//
//        var table = new IcebergTable(namespaceName, tableName);
//        var commit = catalog.updateTable(table, update);
//        var updatedTable = catalog.loadTable(table);
//
//        assertNotEquals(createdTableMetadata.metadata().lastUpdated(), commit.metadata().lastUpdated());
//        assertEquals("value", createdTableMetadata.metadata().properties().get("custom-property"));
//        assertEquals("updated-value", updatedTable.metadata().properties().get("custom-property"));
//    }
//
//    @Test
//    public void successfullyLoadTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table9");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                new IcebergValues.Location("location"),
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of()
//        );
//
//        var createdTable = catalog.createTable(rq);
//        var table = new IcebergTable(namespaceName, tableName);
//        var loadedTable = catalog.loadTable(table);
//
//        assertEquals(createdTable.metadata().uuid(), loadedTable.metadata().uuid());
//    }
//
//    @Test
//    @Disabled() // todo: fix it
//    public void successfullyRegisterTable() {
//        var namespaceName = new IcebergNamespace.Name("ns_table10");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var registeredTableName = new IcebergTable.Name("registered-table");
//
//        var location = new IcebergValues.Location("location");
//
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                location,
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of()
//        );
//
//        catalog.createTable(rq);
//
//        var table = new IcebergTable(namespaceName, registeredTableName);
//        var registeredTable = catalog.registerTable(table, location);
//    }
//
//    @Test
//    public void successfullyCommitTransaction() {
//        var namespaceName = new IcebergNamespace.Name("ns_tx1");
//        var namespace = new IcebergNamespace(namespaceName, Map.of());
//        catalog.createNamespace(namespace);
//
//        var tableName = new IcebergTable.Name("table");
//        var location = new IcebergValues.Location("location");
//
//        var rq = new IcebergTable.CreateRequest(
//                namespaceName,
//                tableName,
//                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
//                location,
//                new IcebergTable.PartitionSpecification.Unpartitioned(),
//                new IcebergTable.SortSpecification.Unsorted(),
//                Map.of()
//        );
//
//        var table = new IcebergTable(namespaceName, tableName);
//        var createdTable = catalog.createTable(rq);
//
//        var transaction = List.of(
//                new IcebergTable.Transaction(
//                        table,
//                        new IcebergTable.UpdateRequest(
//                                List.of(
//                                        new IcebergTable.UpdateRequest.Requirement.AssertTableUuid(createdTable.metadata().uuid())
//                                ),
//                                List.of(
//                                        new IcebergTable.UpdateRequest.Update.SetPropertiesUpdate(
//                                                Map.of("transaction-property", "value")
//                                        )
//                                )
//                        )
//                )
//        );
//
//        catalog.commitTransaction(transaction);
//
//        var loadedTable = catalog.loadTable(table);
//
//        assertEquals("value", loadedTable.metadata().properties().get("transaction-property"));
//    }
}
