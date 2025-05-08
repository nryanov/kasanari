package kasanari.catalog.iceberg.core;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IcebergCatalogAdapterTest {
    private IcebergCatalogAdapter catalogAdapter;

    @BeforeAll
    abstract public void setup();

    @AfterAll
    abstract public void close();

    @BeforeEach
    abstract public void reset();

    @Test
    public void foo() {

    }
}
