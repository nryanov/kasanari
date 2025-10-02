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
import org.apache.iceberg.rest.requests.UpdateTableRequest;
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
public abstract class IcebergCatalogTableApiTest {
    protected IcebergCatalogAdapter catalog;

    abstract public IcebergCatalogAdapter setupCatalog();

    abstract public String entityLocation(String name);

    abstract public String entityName();

    @BeforeAll
    public final void setup() {
        catalog = setupCatalog();
    }

    @AfterAll
    public void close() {}

    @BeforeEach
    public final void beforeEach() {
        reset();
    }

    public void reset() {}

    @Test
    public void returnFalseIfTableDoesNotExist() {
        var namespace = Namespace.empty();
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);

        assertFalse(catalog.tableExists(table));
    }

    @Test
    public void successfullyCreateUnpartitionedAndUnsortedTable() {
        var namespace = Namespace.empty();
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var newTable = TableIdentifier.of(namespace, "newTable");
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
        var tableName = entityName();
        var table = TableIdentifier.of(namespace, tableName);
        var rq = CreateTableRequest
                .builder()
                .withName(tableName)
                .withLocation(entityLocation(tableName))
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
