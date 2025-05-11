package kasanari.catalog.iceberg.core;

import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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
    public void reset() {

    }

    @Test
    public void returnEmptyNamespaceList() {
        var filter = new IcebergNamespace.Listing.Filter(
                Optional.empty(),
                Optional.of(String.valueOf(Integer.MAX_VALUE - 15)),
                Optional.of(10)
        );
        var result = catalog.listNamespaces(filter);

        assertTrue(result.namespaces().isEmpty());
    }

    @Test
    public void returnFalseIfNamespaceDoesNotExist() {
        var namespaceName = new IcebergNamespace.Name("ns1");
        var result = catalog.namespaceExists(namespaceName);

        assertFalse(result);
    }

    @Test
    public void successfullyCreateNamespace() {
        var namespaceName = new IcebergNamespace.Name("ns2");
        var namespace = new IcebergNamespace(namespaceName, Map.of());
        catalog.createNamespace(namespace);
        var result = catalog.namespaceExists(namespaceName);

        assertTrue(result);
    }

    @Test
    public void successfullyDeleteExistingNamespace() {
        var namespaceName = new IcebergNamespace.Name("ns3");
        var namespace = new IcebergNamespace(namespaceName, Map.of());
        catalog.createNamespace(namespace);
        var result = catalog.namespaceExists(namespaceName);

        assertTrue(result);

        catalog.dropNamespace(namespaceName);

        var resultAfterDeleting = catalog.namespaceExists(namespaceName);

        assertFalse(resultAfterDeleting);
    }

    @Test
    public void returnNonEmptyNamespaceList() {
        var namespaceName = new IcebergNamespace.Name("ns4");
        var namespace = new IcebergNamespace(namespaceName, Map.of());
        catalog.createNamespace(namespace);

        var filter = new IcebergNamespace.Listing.Filter();
        var result = catalog.listNamespaces(filter);

        assertFalse(result.namespaces().isEmpty());
    }

    @Test
    public void successfullyLoadNamespace() {
        var namespaceName = new IcebergNamespace.Name("ns5");
        var namespace = new IcebergNamespace(namespaceName, Map.of());
        catalog.createNamespace(namespace);
        var loadedNamespace = catalog.loadNamespaceMetadata(namespaceName);

        assertEquals(namespace, loadedNamespace);
    }

    @Test
    public void successfullyUpdateNamespaceProperties() {
        var namespaceName = new IcebergNamespace.Name("ns6");
        var namespace = new IcebergNamespace(namespaceName, Map.of(
                "property1", "value1",
                "property2", "value2",
                "property3", "value3"
        ));
        catalog.createNamespace(namespace);

        var rq = new IcebergNamespace.Update(Set.of("property2"), Map.of("property4", "value4"));
        catalog.updateNamespace(namespaceName, rq);

        var loadedNamespace = catalog.loadNamespaceMetadata(namespaceName);
        var expectedNamespace = new IcebergNamespace(namespaceName, Map.of(
                "property1", "value1",
                "property3", "value3",
                "property4", "value4"
        ));

        assertEquals(expectedNamespace, loadedNamespace);
    }
}
