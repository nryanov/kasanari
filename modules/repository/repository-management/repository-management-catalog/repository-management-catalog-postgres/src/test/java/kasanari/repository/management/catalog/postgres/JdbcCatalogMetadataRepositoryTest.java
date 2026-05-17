package kasanari.repository.management.catalog.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.repository.management.catalog.model.CatalogMode;
import kasanari.repository.management.catalog.model.CatalogSpec;
import kasanari.repository.management.common.model.CatalogType;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcCatalogMetadataRepositoryTest {

    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static PostgresHelper postgresHelper;
    private static Jdbi jdbi;
    private static JdbcCatalogMetadataRepository repository;

    @BeforeAll
    static void setup() throws Exception {
        POSTGRES.start();
        postgresHelper = new PostgresHelper(POSTGRES);
        repository = new JdbcCatalogMetadataRepository(new ObjectMapper());
    }

    @AfterAll
    static void cleanup() {
        POSTGRES.stop();
    }

    @AfterEach
    void afterEach() {
        postgresHelper.truncateTable("kasanari_catalogs");
    }

    @BeforeEach
    void beforeEach() {
        if (jdbi == null) {
            jdbi = Jdbi.create(POSTGRES.jdbcUrl(), POSTGRES.username(), POSTGRES.password());
            jdbi.useHandle(h -> h.createUpdate(JdbcManagementCatalogQueries.CREATE_CATALOG_REGISTRY_DDL).execute());
        }
    }

    @Test
    void createThenGetByNameReturnsRow() {
        var spec = catalogSpec();
        var inserted = new CatalogMetadata("warehouse", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L);

        jdbi.inTransaction(tx -> {
            repository.create(tx, inserted);
            return null;
        });

        var result = jdbi.inTransaction(tx -> repository.getByName(tx, CatalogType.ICEBERG, "warehouse"));

        var expected = new CatalogMetadata("warehouse", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L);
        assertEquals(expected, result.orElseThrow());
    }

    @Test
    void createDuplicateTypeAndNameReturnsFalse() {
        var metadata = new CatalogMetadata("dup", CatalogType.ICEBERG, CatalogMode.INTERNAL, catalogSpec(), 1L);

        var firstThenSecond = jdbi.inTransaction(tx -> List.of(
                repository.create(tx, metadata),
                repository.create(tx, metadata)));

        assertEquals(List.of(true, false), firstThenSecond);
    }

    @Test
    void sameNameDifferentTypesCoexist() {
        var coexistence = jdbi.inTransaction(tx -> {
            repository.create(tx, new CatalogMetadata(
                    "shared", CatalogType.ICEBERG, CatalogMode.INTERNAL, catalogSpec(), 1L));
            repository.create(tx, new CatalogMetadata(
                    "shared", CatalogType.PAIMON, CatalogMode.INTERNAL, catalogSpec(), 1L));
            return new BothTypesPresent(
                    repository.getByName(tx, CatalogType.ICEBERG, "shared").isPresent(),
                    repository.getByName(tx, CatalogType.PAIMON, "shared").isPresent());
        });

        assertEquals(new BothTypesPresent(true, true), coexistence);
    }

    @Test
    void getByNameMissingReturnsEmpty() {
        var result = jdbi.inTransaction(tx -> repository.getByName(tx, CatalogType.LANCE, "nope"));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void updateBumpsVersionAndPersistsSpec() {
        var insertedSpec = catalogSpec();
        jdbi.inTransaction(tx -> {
            repository.create(tx, new CatalogMetadata("c", CatalogType.ICEBERG, CatalogMode.INTERNAL, insertedSpec, 1L));
            return null;
        });

        var newSpec = catalogSpec();

        var updated = jdbi.inTransaction(tx -> repository.update(tx, CatalogType.ICEBERG, "c", newSpec, 1L));

        var expectedAfterUpdate = new CatalogMetadata("c", CatalogType.ICEBERG, CatalogMode.INTERNAL, newSpec, 2L);
        assertEquals(expectedAfterUpdate, updated.orElseThrow());

        var loaded = jdbi.inTransaction(tx -> repository.getByName(tx, CatalogType.ICEBERG, "c"));
        assertEquals(expectedAfterUpdate, loaded.orElseThrow());
    }

    @Test
    void updateWithStaleExpectedVersionThrows() {
        jdbi.inTransaction(tx -> {
            repository.create(tx, new CatalogMetadata("v", CatalogType.ICEBERG, CatalogMode.INTERNAL, catalogSpec(), 1L));
            return null;
        });

        assertThrows(IllegalStateException.class, () -> jdbi.inTransaction(tx ->
                repository.update(tx, CatalogType.ICEBERG, "v", catalogSpec(), 99L)));
    }

    @Test
    void deleteRemovesRow() {
        jdbi.inTransaction(tx -> {
            repository.create(tx, new CatalogMetadata("d", CatalogType.ICEBERG, CatalogMode.INTERNAL, catalogSpec(), 1L));
            return null;
        });

        var deleted = jdbi.inTransaction(tx -> repository.delete(tx, CatalogType.ICEBERG, "d"));

        assertEquals(true, deleted);

        var afterDelete = jdbi.inTransaction(tx -> repository.getByName(tx, CatalogType.ICEBERG, "d"));

        assertEquals(Optional.empty(), afterDelete);
    }

    @Test
    void deleteMissingReturnsFalse() {
        var deleted = jdbi.inTransaction(tx -> repository.delete(tx, CatalogType.ICEBERG, "missing"));

        assertEquals(false, deleted);
    }

    @Test
    void listByTypeReturnsOnlyMatchingCatalogs() {
        var spec = catalogSpec();
        jdbi.inTransaction(tx -> {
            repository.create(tx, new CatalogMetadata("alpha", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L));
            repository.create(tx, new CatalogMetadata("beta", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L));
            repository.create(tx, new CatalogMetadata("gamma", CatalogType.PAIMON, CatalogMode.INTERNAL, spec, 1L));
            repository.create(tx, new CatalogMetadata("delta", CatalogType.LANCE, CatalogMode.INTERNAL, spec, 1L));
            return null;
        });

        var icebergCatalogs = jdbi.inTransaction(tx -> repository.list(tx, CatalogType.ICEBERG));

        assertEquals(List.of(
                new CatalogMetadata("alpha", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L),
                new CatalogMetadata("beta", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L)
        ), icebergCatalogs);
    }

    @Test
    void listByTypeEmptyWhenNone() {
        var result = jdbi.inTransaction(tx -> repository.list(tx, CatalogType.LANCE));

        assertEquals(List.of(), result);
    }

    private static CatalogSpec catalogSpec() {
        return new CatalogSpec(new HashMap<>(), new HashMap<>());
    }

    private record BothTypesPresent(boolean icebergPresent, boolean paimonPresent) {
    }
}
