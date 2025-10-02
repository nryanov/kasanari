package kasanari.catalog.iceberg.core;

import org.apache.iceberg.catalog.Namespace;
import org.junit.jupiter.api.AfterAll;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IcebergCatalogNamespaceApiTest {
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
        catalog.createNamespace(namespace, new HashMap<>(Map.of("prop1", "value")));
        var loadedNamespace = catalog.loadNamespaceMetadata(namespace);

        var expectedProps = new HashMap<>(Map.of("prop1", "value", "location", "s3a://warehouse/ns5"));

        assertEquals(expectedProps, loadedNamespace.properties());
        assertEquals(namespace, loadedNamespace.namespace());
    }

    @Test
    public void successfullyUpdateNamespaceProperties() {
        var namespace = Namespace.of("ns6");
        var properties = new HashMap<>(Map.of(
                "property1", "value1",
                "property2", "value2",
                "property3", "value3"
        ));
        catalog.createNamespace(namespace, properties);

        catalog.updateNamespace(namespace, new HashMap<>(Map.of("property4", "value4")), new HashSet<>(Set.of("property2")));

        var loadedNamespace = catalog.loadNamespaceMetadata(namespace);
        var expectedProperties = Map.of(
                "property1", "value1",
                "property3", "value3",
                "property4", "value4",
                "location", "s3a://warehouse/ns6"
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
    public void returnEmptyTableListing() {
        var namespace = Namespace.empty();
        var result = catalog.listTables(namespace, null, 10);

        assertTrue(result.identifiers().isEmpty());
    }

    @Test
    public void returnEmptyViewListing() {
        var namespace = Namespace.empty();
        var result = catalog.listViews(namespace, null, 10);

        assertTrue(result.identifiers().isEmpty());
    }
}
