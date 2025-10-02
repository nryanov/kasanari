package kasanari.catalog.iceberg.core;

import org.apache.iceberg.MetadataUpdate;
import org.apache.iceberg.UpdateRequirement;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.ImmutableCreateViewRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.view.ImmutableSQLViewRepresentation;
import org.apache.iceberg.view.ImmutableViewVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IcebergCatalogViewApiTest {
    protected IcebergCatalogAdapter catalog;

    abstract public String entityLocation(String name);

    abstract public String entityName();

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
        var viewName = entityName();
        var view = TableIdentifier.of(namespace, viewName);
        var result = catalog.viewExists(view);

        assertFalse(result);
    }

    @Test
    public void successfullyCreateView() {
        var namespace = Namespace.empty();
        var viewName = entityName();
        var view = TableIdentifier.of(namespace, viewName);
        var rq = ImmutableCreateViewRequest
                .builder()
                .name(viewName)
                .location(entityLocation(viewName))
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
        var viewName = entityName();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(viewName));
        var view = TableIdentifier.of(namespace, viewName);

        catalog.createView(namespace, rq);

        assertTrue(catalog.viewExists(view));
        catalog.dropView(view);
        assertFalse(catalog.viewExists(view));
    }

    @Test
    public void returnNonEmptyListOfViews() {
        var namespace = Namespace.empty();
        var viewName = entityName();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(viewName));
        var view = TableIdentifier.of(namespace, viewName);

        catalog.createView(namespace, rq);

        var result = catalog.listViews(namespace, null, 10);

        var expectedViews = List.of(view);
        assertEquals(expectedViews, result.identifiers());
    }

    @Test
    public void successfullyRenameView() {
        var namespace = Namespace.empty();
        var viewName = entityName();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(viewName));
        var view = TableIdentifier.of(namespace, viewName);
        var newViewName = TableIdentifier.of(namespace, "renamed_view");

        catalog.createView(namespace, rq);


        catalog.renameView(view, newViewName);

        assertFalse(catalog.viewExists(view));
        assertTrue(catalog.viewExists(newViewName));
    }

    @Test
    public void successfullyLoadView() {
        var namespace = Namespace.empty();
        var viewName = entityName();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(viewName));
        var view = TableIdentifier.of(namespace, viewName);

        catalog.createView(namespace, rq);

        var result = catalog.loadView(view);

        assertEquals(1, result.metadata().currentVersionId());
        assertEquals("s3a://warehouse/" + viewName, result.metadata().location());
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
        var viewName = entityName();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(viewName));
        var view = TableIdentifier.of(namespace, viewName);

        var createdView = catalog.createView(namespace, rq);

        var updateRq = UpdateTableRequest
                .create(
                        view,
                        List.of(
                                new UpdateRequirement.AssertViewUUID(createdView.metadata().uuid())
                        ),
                        List.of(
                                new MetadataUpdate.SetLocation("s3://warehouse/newLocation")
                        )
                );

        catalog.replaceView(view, updateRq);

        var result = catalog.loadView(view);

        assertEquals("s3://warehouse/newLocation", result.metadata().location());
    }
}
