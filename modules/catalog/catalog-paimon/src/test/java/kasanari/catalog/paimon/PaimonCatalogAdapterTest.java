package kasanari.catalog.paimon;

import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.rest.requests.CreateDatabaseRequest;
import org.apache.paimon.rest.requests.CreateTableRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.types.DataTypes;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PaimonCatalogAdapterTest {
    protected PaimonCatalogAdapter catalog;

    @BeforeAll
    public final void setup() {
        this.catalog = setupCatalogAdapter();
    }

    protected abstract PaimonCatalogAdapter setupCatalogAdapter();

    protected abstract String databaseName();

    protected abstract String tableName();

    protected String renamedTableName(String sourceName) {
        return sourceName + "_renamed";
    }

    protected boolean isTableRenameSupported() {
        return true;
    }

    @AfterAll
    public final void afterAll() {
        close();
    }

    protected void close() {
    }

    @BeforeEach
    public final void beforeEach() {
        reset();
    }

    protected void reset() {
    }

    @Test
    public void returnEmptyDatabaseList() {
        var result = catalog.listDatabases("test", 10, null);
        assertTrue(result.getDatabases().isEmpty());
    }

    @Test
    public void successfullyCreateGetAndDropDatabase() {
        var db = databaseName();
        var createRequest = new CreateDatabaseRequest(db, Map.of("owner", "kasanari"));

        catalog.createDatabase("test", createRequest);

        var loaded = catalog.getDatabase("test", db);
        assertEquals(db, loaded.getName());
        assertEquals("kasanari", loaded.getOptions().get("owner"));

        catalog.dropDatabase("test", db);

        var databasesAfterDrop = catalog.listDatabases("test", 100, null).getDatabases();
        assertFalse(databasesAfterDrop.contains(db));
    }

    @Test
    public void returnEmptyTableListing() {
        var db = databaseName();
        catalog.createDatabase("test", new CreateDatabaseRequest(db, Map.of()));

        var result = catalog.listTables("test", db, 10, null, null);
        assertTrue(result.getTables().isEmpty());
    }

    @Test
    public void successfullyCreateGetAndDropTable() {
        var db = databaseName();
        catalog.createDatabase("test", new CreateDatabaseRequest(db, Map.of()));

        var table = tableName();
        var identifier = Identifier.create(db, table);
        var createTableRequest = new CreateTableRequest(identifier, defaultSchema());

        catalog.createTable("test", db, createTableRequest);

        var loaded = catalog.getTable("test", db, table);
        assertEquals(table, loaded.getName());
        assertEquals(db, loaded.getDatabase());

        catalog.dropTable("test", db, table);

        var tablesAfterDrop = catalog.listTables("test", db, 100, null, null).getTables();
        assertFalse(tablesAfterDrop.contains(table));
    }

    @Test
    public void successfullyRenameTable() {
        Assumptions.assumeTrue(this::isTableRenameSupported, "Table rename is not supported by this catalog.");

        var db = databaseName();
        catalog.createDatabase("test", new CreateDatabaseRequest(db, Map.of()));

        var source = tableName();
        var target = renamedTableName(source);

        catalog.createTable("test", db, new CreateTableRequest(Identifier.create(db, source), defaultSchema()));
        catalog.renameTable("test", new RenameTableRequest(Identifier.create(db, source), Identifier.create(db, target)));

        var tables = catalog.listTables("test", db, 100, null, null).getTables();
        assertFalse(tables.contains(source));
        assertTrue(tables.contains(target));
    }

    @Test
    public void supportsPagedTableListing() {
        var db = databaseName();
        catalog.createDatabase("test", new CreateDatabaseRequest(db, Map.of()));

        var table1 = tableName();
        var table2 = renamedTableName(table1);

        catalog.createTable("test", db, new CreateTableRequest(Identifier.create(db, table1), defaultSchema()));
        catalog.createTable("test", db, new CreateTableRequest(Identifier.create(db, table2), defaultSchema()));

        var page1 = catalog.listTables("test", db, 1, null, null);
        assertNotNull(page1.getTables());
        assertFalse(page1.getTables().isEmpty());

        var discoveredTables = new HashSet<>(page1.getTables());
        if (page1.getNextPageToken() != null) {
            var page2 = catalog.listTables("test", db, 1, page1.getNextPageToken(), null);
            discoveredTables.addAll(page2.getTables());
        } else {
            // Some catalog implementations may ignore page size and return all entries in one page.
            discoveredTables.addAll(catalog.listTables("test", db, 100, null, null).getTables());
        }

        assertTrue(discoveredTables.contains(table1));
        assertTrue(discoveredTables.contains(table2));
    }

    private Schema defaultSchema() {
        return Schema
                .newBuilder()
                .column("id", DataTypes.INT())
                .column("name", DataTypes.STRING())
                .primaryKey("id")
                .options(new HashMap<>(Map.of("bucket", "1")))
                .build();
    }
}
