package kasanari.catalog.iceberg;

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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IcebergCatalogAdapterTest {
    protected IcebergCatalogAdapter catalog;

    private final AtomicInteger uniqueId = new AtomicInteger();

    @BeforeAll
    public final void setup() {
        catalog = setupCatalog();
    }

    abstract public IcebergCatalogAdapter setupCatalog();

    abstract public String entityLocation(TableIdentifier identifier);

    protected final String nextUniqueSuffix() {
        return String.valueOf(uniqueId.incrementAndGet());
    }

    protected String uniqueNamespaceName() {
        return "ns_" + nextUniqueSuffix();
    }

    protected String uniqueTableName() {
        return "table_" + nextUniqueSuffix();
    }

    protected String uniqueViewName() {
        return "view_" + nextUniqueSuffix();
    }

    protected String tableName() {
        return uniqueTableName();
    }

    protected String viewName() {
        return uniqueViewName();
    }

    protected boolean usesDefaultEmptyNamespace() {
        return false;
    }

    protected void ensureNamespaceExists(Namespace namespace) {
        if (isNamespaceSupported() && !catalog.namespaceExists(namespace)) {
            catalog.createNamespace(namespace);
        }
    }

    protected Namespace namespaceName() {
        if (usesDefaultEmptyNamespace()) {
            return Namespace.empty();
        }
        var namespace = Namespace.of(uniqueNamespaceName());
        ensureNamespaceExists(namespace);
        return namespace;
    }

    protected TableIdentifier viewIdentifier(Namespace namespace, String viewName) {
        return TableIdentifier.of(namespace, viewName);
    }

    public boolean isNamespaceSupported() {
        return true;
    }

    public boolean isViewSupported() {
        return true;
    }

    public boolean isRegisterTableSupported() {
        return true;
    }

    public boolean isRegisterViewSupported() {
        return isViewSupported();
    }

    @AfterAll
    public final void afterAll() {
        close();
    }

    public void close() {}

    @BeforeEach
    public final void beforeEach() {
        reset();
    }

    public void reset() {}

    @Test
    public void returnEmptyNamespaceList() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var result = catalog.listNamespaces(String.valueOf(Integer.MAX_VALUE - 15), 10, null);

        assertTrue(result.namespaces().isEmpty());
    }

    @Test
    public void returnFalseIfNamespaceDoesNotExist() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespaceName = Namespace.of(uniqueNamespaceName());
        var result = catalog.namespaceExists(namespaceName);

        assertFalse(result);
    }

    @Test
    public void successfullyCreateNamespace() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespace = Namespace.of(uniqueNamespaceName());
        var properties = new HashMap<>(Map.of("prop1", "value"));
        var response = catalog.createNamespace(namespace, properties);
        var result = catalog.namespaceExists(namespace);

        assertTrue(result);
        assertEquals(namespace, response.namespace());
        assertEquals("value", response.properties().get("prop1"));
    }

    @Test
    public void successfullyDeleteExistingNamespace() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespace = Namespace.of(uniqueNamespaceName());
        catalog.createNamespace(namespace);
        var result = catalog.namespaceExists(namespace);

        assertTrue(result);

        catalog.dropNamespace(namespace);

        var resultAfterDeleting = catalog.namespaceExists(namespace);

        assertFalse(resultAfterDeleting);
    }

    @Test
    public void returnNonEmptyNamespaceList() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespace = Namespace.of(uniqueNamespaceName());
        catalog.createNamespace(namespace);

        var result = catalog.listNamespaces(null, 10, null);

        assertFalse(result.namespaces().isEmpty());
    }

    @Test
    public void successfullyLoadNamespace() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespace = Namespace.of(uniqueNamespaceName());
        catalog.createNamespace(namespace, new HashMap<>(Map.of("prop1", "value")));
        var loadedNamespace = catalog.loadNamespaceMetadata(namespace);

        assertEquals("value", loadedNamespace.properties().get("prop1"));
        assertEquals(namespace, loadedNamespace.namespace());
    }

    @Test
    public void successfullyUpdateNamespaceProperties() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");

        var namespace = Namespace.of(uniqueNamespaceName());
        var properties = new HashMap<>(Map.of(
                "property1", "value1",
                "property2", "value2",
                "property3", "value3"
        ));
        catalog.createNamespace(namespace, properties);

        var response = catalog.updateNamespace(
                namespace,
                new HashMap<>(Map.of("property4", "value4")),
                new HashSet<>(Set.of("property2"))
        );
        var loadedNamespace = catalog.loadNamespaceMetadata(namespace);
        var loadedProperties = loadedNamespace.properties();

        assertEquals("value1", loadedProperties.get("property1"));
        assertFalse(loadedProperties.containsKey("property2"));
        assertEquals("value3", loadedProperties.get("property3"));
        assertEquals("value4", loadedProperties.get("property4"));

        assertEquals(List.of("property4"), response.updated());
        assertEquals(List.of("property2"), response.removed());
    }

    @Test
    public void correctlyReturnMissingNamespaceProperties() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");

        var namespace = Namespace.of(uniqueNamespaceName());
        catalog.createNamespace(namespace, new HashMap<>(Map.of(
                "property1", "value1",
                "property2", "value2"
        )));

        var response = catalog.updateNamespace(
                namespace,
                new HashMap<>(Map.of("property1", "new-value1")),
                new HashSet<>(Set.of("property2", "missing-property"))
        );

        assertEquals(List.of("property1"), response.updated());
        assertEquals(List.of("property2"), response.removed());
        assertEquals(List.of("missing-property"), response.missing());
    }

    @Test
    public void correctlyHandleNamespacePropertyUpdateWhenUpdatedPropertyShouldBeRemoved() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");

        var namespace = Namespace.of(uniqueNamespaceName());
        catalog.createNamespace(namespace, new HashMap<>(Map.of(
                "property1", "value1"
        )));

        var response = catalog.updateNamespace(
                namespace,
                new HashMap<>(Map.of("property1", "new-value1")),
                new HashSet<>(Set.of("property1"))
        );

        assertEquals(List.of(), response.updated());
        assertEquals(List.of("property1"), response.removed());
        assertEquals(List.of(), response.missing());
    }

    @Test
    public void correctlyPaginateNamespaceListing() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var parent = uniqueNamespaceName();
        var namespaceParent = Namespace.of(parent);
        var namespace1 = Namespace.of(parent, "1");
        var namespace2 = Namespace.of(parent, "2");

        catalog.createNamespace(namespaceParent);
        catalog.createNamespace(namespace1);
        catalog.createNamespace(namespace2);

        var page1 = catalog.listNamespaces(null, 1, parent);

        assertEquals(List.of(namespace1), page1.namespaces());

        var page2 = catalog.listNamespaces(page1.nextPageToken(), 1, parent);
        assertEquals(List.of(namespace2), page2.namespaces());
        assertNotNull(page1.nextPageToken());
        assertNull(page2.nextPageToken());
    }

    @Test
    public void returnAllNamespacesWhenPageSizeExceedsTotal() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var parent = uniqueNamespaceName();
        var namespaceParent = Namespace.of(parent);
        var namespace1 = Namespace.of(parent, "a");
        var namespace2 = Namespace.of(parent, "b");

        catalog.createNamespace(namespaceParent);
        catalog.createNamespace(namespace1);
        catalog.createNamespace(namespace2);

        var result = catalog.listNamespaces(null, 100, parent);

        assertEquals(Set.of(namespace1, namespace2), new HashSet<>(result.namespaces()));
        assertNull(result.nextPageToken());
    }

    @Test
    public void returnEmptyNamespacePageWhenPageTokenBeyondTotal() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespace = Namespace.of(uniqueNamespaceName());
        catalog.createNamespace(namespace);

        var result = catalog.listNamespaces("100", 10, null);

        assertTrue(result.namespaces().isEmpty());
        assertNull(result.nextPageToken());
    }

    @Test
    public void returnEmptyNamespacePageWhenPageSizeIsZero() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespace = Namespace.of(uniqueNamespaceName());
        catalog.createNamespace(namespace);

        var result = catalog.listNamespaces(null, 0, null);

        assertTrue(result.namespaces().isEmpty());
        assertNotNull(result.nextPageToken());
    }

    @Test
    public void returnEmptyTableListing() {
        Assumptions.assumeTrue(this::isNamespaceSupported, "Test skipped: namespaces are not supported in this catalog");
        var namespace = namespaceName();
        var result = catalog.listTables(namespace, null, 10);

        assertTrue(result.identifiers().isEmpty());
    }

    @Test
    public void returnNonEmptyTableList() {
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        catalog.createTable(namespace, createTableRequest(table));

        var result = catalog.listTables(namespace, null, 10);

        assertEquals(List.of(table), result.identifiers());
    }

    @Test
    public void correctlyPaginateTableListing() {
        var namespace = namespaceName();
        var table1Name = uniqueTableName();
        var table2Name = uniqueTableName();

        var table1 = TableIdentifier.of(namespace, table1Name);
        var table2 = TableIdentifier.of(namespace, table2Name);

        catalog.createTable(namespace, createTableRequest(table1));
        catalog.createTable(namespace, createTableRequest(table2));

        var allTables = catalog.listTables(namespace, null, 100).identifiers();

        var page1 = catalog.listTables(namespace, null, 1);
        assertEquals(allTables.subList(0, 1), page1.identifiers());
        assertNotNull(page1.nextPageToken());

        var page2 = catalog.listTables(namespace, page1.nextPageToken(), 1);
        assertEquals(allTables.subList(1, 2), page2.identifiers());

//        may be not null
//        assertNull(page2.nextPageToken());
    }

    @Test
    public void returnEmptyTablePageWhenPageTokenBeyondTotal() {
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);

        catalog.createTable(namespace, createTableRequest(table));

        var result = catalog.listTables(namespace, "100", 10);

        assertTrue(result.identifiers().isEmpty());
        assertNull(result.nextPageToken());
    }

    @Test
    public void returnFalseIfTableDoesNotExist() {
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);

        assertFalse(catalog.tableExists(table));
    }

    @Test
    public void successfullyCreateUnpartitionedAndUnsortedTable() {
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        catalog.createTable(namespace, rq);

        assertTrue(catalog.tableExists(table));
    }

    @Test
    public void successfullyCreatePartitionedTable() {
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
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
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
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
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
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
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var newTable = TableIdentifier.of(namespace, uniqueTableName());
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
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
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
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
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createdTable = catalog.createTable(namespace, rq);
        var loadedTable = catalog.loadTable(table);

        assertEquals(createdTable.tableMetadata().uuid(), loadedTable.tableMetadata().uuid());
    }

    @Test
    public void successfullyRegisterExistingTable() {
        Assumptions.assumeTrue(this::isRegisterTableSupported, "Test skipped: registerTable is not supported in this catalog");
        var namespace = namespaceName();
        var registeredTableName = uniqueTableName();
        var table = TableIdentifier.of(namespace, registeredTableName);
        var createdTable = catalog.createTable(namespace, createTableRequest(table));
        var metadataLocation = createdTable.tableMetadata().metadataFileLocation();

        catalog.dropTable(table, false);
        assertFalse(catalog.tableExists(table));

        var registeredTable = catalog.registerTable(table, metadataLocation);

        assertTrue(catalog.tableExists(table));
        assertEquals(createdTable.tableMetadata().uuid(), registeredTable.tableMetadata().uuid());
    }

    @Test
    public void successfullyRegisterExistingView() {
        Assumptions.assumeTrue(this::isRegisterViewSupported, "Test skipped: registerView is not supported in this catalog");
        var namespace = namespaceName();
        var registeredViewName = uniqueViewName();
        var view = viewIdentifier(namespace, registeredViewName);
        var createdView = catalog.createView(
                namespace,
                IcebergCatalogCommons.defaultCreateViewRequest(namespace, registeredViewName, entityLocation(view))
        );
        var metadataLocation = createdView.metadata().metadataFileLocation();

        catalog.dropView(view);
        assertFalse(catalog.viewExists(view));

        var registeredView = catalog.registerView(view, metadataLocation);

        assertTrue(catalog.viewExists(view));
        assertEquals(createdView.metadata().uuid(), registeredView.metadata().uuid());
    }

    @Test
    public void successfullyCommitTransaction() {
        var namespace = namespaceName();
        var tableName = tableName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(table))
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

    @Test
    public void returnEmptyViewListing() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var result = catalog.listViews(namespace, null, 10);

        assertTrue(result.identifiers().isEmpty());
    }

    @Test
    public void returnFalseIfViewDoesNotExist() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        var result = catalog.viewExists(view);

        assertFalse(result);
    }

    @Test
    public void successfullyCreateView() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        var rq = ImmutableCreateViewRequest
                .builder()
                .name(viewName)
                .location(entityLocation(view))
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
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(view));

        catalog.createView(namespace, rq);

        assertTrue(catalog.viewExists(view));
        catalog.dropView(view);
        assertFalse(catalog.viewExists(view));
    }

    @Test
    public void returnNonEmptyListOfViews() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(view));

        catalog.createView(namespace, rq);

        var result = catalog.listViews(namespace, null, 10);

        var expectedViews = List.of(view);
        assertEquals(expectedViews, result.identifiers());
    }

    @Test
    public void correctlyPaginateViewListing() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var view1Name = uniqueViewName();
        var view2Name = uniqueViewName();

        var view1 = viewIdentifier(namespace, view1Name);
        var view2 = viewIdentifier(namespace, view2Name);

        catalog.createView(namespace, IcebergCatalogCommons.defaultCreateViewRequest(namespace, view1Name, entityLocation(view1)));
        catalog.createView(namespace, IcebergCatalogCommons.defaultCreateViewRequest(namespace, view2Name, entityLocation(view2)));

        var allViews = catalog.listViews(namespace, null, 100).identifiers();

        var page1 = catalog.listViews(namespace, null, 1);
        assertEquals(allViews.subList(0, 1), page1.identifiers());
        assertNotNull(page1.nextPageToken());

        var page2 = catalog.listViews(namespace, page1.nextPageToken(), 1);
        assertEquals(allViews.subList(1, 2), page2.identifiers());

//        may be not null
//        assertNull(page2.nextPageToken());
    }

    @Test
    public void returnEmptyViewPageWhenPageTokenBeyondTotal() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        catalog.createView(namespace, IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(view)));

        var result = catalog.listViews(namespace, "100", 10);

        assertTrue(result.identifiers().isEmpty());
        assertNull(result.nextPageToken());
    }

    @Test
    public void successfullyRenameView() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        var newViewName = viewIdentifier(namespace, uniqueViewName());

        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(view));

        catalog.createView(namespace, rq);


        catalog.renameView(view, newViewName);

        assertFalse(catalog.viewExists(view));
        assertTrue(catalog.viewExists(newViewName));
    }

    @Test
    public void successfullyLoadView() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(view));

        catalog.createView(namespace, rq);

        var result = catalog.loadView(view);

        assertEquals(1, result.metadata().currentVersionId());
        assertEquals(entityLocation(view), result.metadata().location());
        assertEquals(1, result.metadata().formatVersion());

        assertEquals(1, result.metadata().versions().size());
        assertEquals(1, result.metadata().history().size());

        var resultVersion = result.metadata().currentVersion();

        assertEquals(1, resultVersion.representations().size());

        var resultVersionRepresentation = (ImmutableSQLViewRepresentation) resultVersion.representations().getFirst();

        assertEquals("sql", resultVersionRepresentation.type());
        assertEquals("select * from table", resultVersionRepresentation.sql());
        assertEquals("spark", resultVersionRepresentation.dialect());
    }

    @Test
    public void successfullyReplaceView() {
        Assumptions.assumeTrue(this::isViewSupported, "Test skipped: views are not supported in this catalog");
        var namespace = namespaceName();
        var viewName = viewName();
        var view = viewIdentifier(namespace, viewName);
        var newView = viewIdentifier(namespace, uniqueViewName());

        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(view));
        var newLocation = entityLocation(newView);

        var createdView = catalog.createView(namespace, rq);

        var updateRq = UpdateTableRequest
                .create(
                        view,
                        List.of(
                                new UpdateRequirement.AssertViewUUID(createdView.metadata().uuid())
                        ),
                        List.of(
                                new MetadataUpdate.SetLocation(newLocation)
                        )
                );

        catalog.replaceView(view, updateRq);

        var result = catalog.loadView(view);

        assertEquals(newLocation, result.metadata().location());
    }

    protected CreateTableRequest createTableRequest(TableIdentifier tableIdentifier) {
        return CreateTableRequest
                .builder()
                .withName(tableIdentifier.name())
                .withLocation(entityLocation(tableIdentifier))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();
    }
}
