package kasanari.catalog.iceberg.core;

import org.apache.iceberg.MetadataUpdate;
import org.apache.iceberg.NullOrder;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.SortDirection;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.UpdateRequirement;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.ImmutableCreateViewRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.view.ImmutableSQLViewRepresentation;
import org.apache.iceberg.view.ImmutableViewVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IcebergCatalogAdapterWithoutNamespaceSupportTest {
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
    public void returnFalseIfViewDoesNotExist() {
        var namespace = Namespace.empty();

        var view = TableIdentifier.of(namespace, "view");
        var result = catalog.viewExists(view);

        assertFalse(result);
    }

    @Test
    public void returnEmptyViewListing() {
        var namespace = Namespace.empty();
        var result = catalog.listViews(namespace, null, 10);

        assertTrue(result.identifiers().isEmpty());
    }

    @Test
    public void successfullyCreateView() {
        var namespace = Namespace.empty();
        var entityName = "view_1";
        var location = String.format("s3a://warehouse/%s", entityName);

        var view = TableIdentifier.of(namespace, entityName);
        var rq = ImmutableCreateViewRequest
                .builder()
                .name(entityName)
                .location(location)
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
                                .defaultNamespace(namespace)
                                .build()
                )
                .build();
        catalog.createView(namespace, rq);

        var result = catalog.viewExists(view);
        assertTrue(result);
    }


    @Test
    public void successfullyDropView() {
        var namespace = Namespace.empty();
        var entityName = "view_2";
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, entityName);
        var view = TableIdentifier.of(namespace, entityName);
        
        catalog.createView(namespace, rq);

        assertTrue(catalog.viewExists(view));
        catalog.dropView(view);
        assertFalse(catalog.viewExists(view));
    }

    @Test
    public void returnNonEmptyListOfViews() {
        var namespace = Namespace.empty();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, "view");
        var view = TableIdentifier.of(namespace, "view");

        catalog.createView(namespace, rq);

        var result = catalog.listViews(namespace, null, 10);

        var expectedViews = List.of(view);
        assertEquals(expectedViews, result.identifiers());
    }

    @Test
    public void successfullyRenameView() {
        var namespace = Namespace.empty();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, "view");
        var view = TableIdentifier.of(namespace, "view");
        var newViewName = TableIdentifier.of(namespace, "renamed_view");
        
        catalog.createView(namespace, rq);


        catalog.renameView(view, newViewName);

        assertFalse(catalog.viewExists(view));
        assertTrue(catalog.viewExists(newViewName));
    }

    @Test
    public void successfullyLoadView() {
        var namespace = Namespace.empty();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, "view");
        var view = TableIdentifier.of(namespace, "view");

        catalog.createView(namespace, rq);

        var result = catalog.loadView(view);

        assertEquals(1, result.metadata().currentVersionId());
        assertEquals("location", result.metadata().location());
        assertEquals(1, result.metadata().formatVersion());

        assertEquals(1, result.metadata().versions().size());
        assertEquals(1, result.metadata().history().size());

        var resultVersion = result.metadata().currentVersion();

        assertEquals(1, resultVersion.representations().size());

        var resultVersionRepresentation = (ImmutableSQLViewRepresentation) resultVersion.representations().get(0);

        assertEquals("sql", resultVersionRepresentation.type());
        assertEquals("select * from table", resultVersionRepresentation.sql());
        assertEquals("spark", resultVersionRepresentation.dialect());
    }

    @Test
    public void successfullyReplaceView() {
        var namespace = Namespace.empty();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, "view");
        var view = TableIdentifier.of(namespace, "view");
        
        var createdView = catalog.createView(namespace, rq);

        var updateRq = UpdateTableRequest
                .create(
                        view,
                        List.of(
                                new UpdateRequirement.AssertViewUUID(createdView.metadata().uuid())
                        ),
                        List.of(
                                new MetadataUpdate.SetLocation("newLocation")
                        )
                );

        catalog.replaceView(view, updateRq);

        var result = catalog.loadView(view);

        assertEquals("newLocation", result.metadata().location());
    }


    @Test
    public void returnFalseIfTableDoesNotExist() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");

        assertFalse(catalog.tableExists(table));
    }

    @Test
    public void returnEmptyTableListing() {
        var namespace = Namespace.empty();
        var result = catalog.listTables(namespace, null, 10);

        assertTrue(result.identifiers().isEmpty());
    }

    @Test
    public void successfullyCreateUnpartitionedAndUnsortedTable() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        catalog.createTable(namespace, rq);

        assertTrue(catalog.tableExists(table));
    }

    @Test
    public void successfullyCreatePartitionedTable() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(
                        PartitionSpec
                                .builderFor(IcebergCatalogCommons.DEFAULT_SCHEMA)
                                .withSpecId(1)
                                .bucket("id", 16)
                                .build()
                )
                .build();

        catalog.createTable(namespace, rq);

        assertTrue(catalog.tableExists(table));
    }

    @Test
    public void successfullyCreateSortedTable() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .withWriteOrder(SortOrder
                        .builderFor(IcebergCatalogCommons.DEFAULT_SCHEMA)
                        .withOrderId(1)
                        .sortBy("id", SortDirection.ASC, NullOrder.NULLS_FIRST)
                        .build()
                )
                .build();

        catalog.createTable(namespace, rq);

        assertTrue(catalog.tableExists(table));
    }

    @Test
    public void successfullyDropTable() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        catalog.createTable(namespace, rq);

        assertTrue(catalog.tableExists(table));

        catalog.dropTable(table, true);

        assertFalse(catalog.tableExists(table));
    }

    @Test
    public void successfullyRenameTable() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var newTable = TableIdentifier.of(namespace, "newTable");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        catalog.createTable(namespace, rq);

        assertTrue(catalog.tableExists(table));

        catalog.renameTable(table, newTable);

        assertFalse(catalog.tableExists(table));
        assertTrue(catalog.tableExists(newTable));
    }

    @Test
    public void successfullyUpdateTable() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .setProperties(new HashMap<>(Map.of("custom-property", "value")))
                .build();

        var tableMetadata = catalog.createTable(namespace, rq);

        var updateRq = UpdateTableRequest
                .create(
                        table,
                        List.of(new UpdateRequirement.AssertTableUUID(tableMetadata.tableMetadata().uuid())),
                        List.of(new MetadataUpdate.SetProperties(new HashMap<>(Map.of("custom-property", "updated-value"))))
                );

        catalog.updateTable(table, updateRq);
        var updatedTable = catalog.loadTable(table);

        assertNotEquals(tableMetadata.tableMetadata().lastUpdatedMillis(), updatedTable.tableMetadata().lastUpdatedMillis());
        assertEquals("value", tableMetadata.tableMetadata().properties().get("custom-property"));
        assertEquals("updated-value", updatedTable.tableMetadata().properties().get("custom-property"));
    }

    @Test
    public void successfullyLoadTable() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createdTable = catalog.createTable(namespace, rq);
        var loadedTable = catalog.loadTable(table);

        assertEquals(createdTable.tableMetadata().uuid(), loadedTable.tableMetadata().uuid());
    }


    @Test
    public void successfullyCommitTransaction() {
        var namespace = Namespace.empty();
        var table = TableIdentifier.of(namespace, "table");
        var rq = CreateTableRequest
                .builder()
                .withName("table")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        
        var createdTable = catalog.createTable(namespace, rq);

        var transaction = List.of(
                UpdateTableRequest
                        .create(table,
                                List.of(new UpdateRequirement.AssertTableUUID(createdTable.tableMetadata().uuid())),
                                List.of(new MetadataUpdate.SetProperties(new HashMap<>(Map.of("transaction-property", "value"))))
                        )
        );

        catalog.commitTransaction(transaction);

        var loadedTable = catalog.loadTable(table);

        assertEquals("value", loadedTable.tableMetadata().properties().get("transaction-property"));
    }
}
