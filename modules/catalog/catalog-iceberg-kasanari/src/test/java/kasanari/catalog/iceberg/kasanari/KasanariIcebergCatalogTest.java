package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import kasanari.catalog.iceberg.core.IcebergCatalogCommons;
import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergTable;
import kasanari.catalog.iceberg.core.model.IcebergValues;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KasanariIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.
                    parse("postgres:17")
                    .asCompatibleSubstituteFor("postgres")
    )
            .withUsername("postgres")
            .withPassword("postgres")
            .withDatabaseName("kasanari");

    @Override
    public IcebergCatalogAdapter setupCatalog() {
//        postgres.start();

        var factory = new KasanariIcebergCatalogFactory();
//        return factory.create(Map.of(
//                KasanariCatalogProperties.WAREHOUSE, "file:///tmp/iceberg-kasanari-catalog-warehouse",
//                KasanariCatalogProperties.URI, postgres.getJdbcUrl(),
//                KasanariCatalogProperties.USER, postgres.getUsername(),
//                KasanariCatalogProperties.PASSWORD, postgres.getPassword()
//        ));

        return factory.create(Map.of(
                KasanariCatalogProperties.WAREHOUSE, "file:///tmp/iceberg-kasanari-catalog-warehouse",
                KasanariCatalogProperties.URI, "jdbc:postgresql://localhost:5432/postgres",
                KasanariCatalogProperties.USER, "postgres",
                KasanariCatalogProperties.PASSWORD, "postgres"
        ));
    }

    @Override
    public void close() {
//        postgres.close();
    }

    @Override
    public void reset() {
        var catalogDelegate = (KasanariCatalog) catalog.delegate();
        catalogDelegate.getDataSource().getJdbi().useTransaction(tx -> {
            tx.execute("TRUNCATE TABLE kasanari_iceberg_namespace_properties CASCADE");
            tx.execute("TRUNCATE TABLE kasanari_iceberg_tables CASCADE");
            tx.execute("TRUNCATE TABLE kasanari_iceberg_views CASCADE");
            tx.execute("TRUNCATE TABLE kasanari_iceberg_namespaces CASCADE");
        });
    }

    @Test
    public void successfullyCommitMultiTableTransaction() {
        var namespaceName = new IcebergNamespace.Name("ns_multi_table_tx1");
        var namespace = new IcebergNamespace(namespaceName, Map.of());
        catalog.createNamespace(namespace);

        var tableNameOne = new IcebergTable.Name("table_1");
        var tableNameTwo = new IcebergTable.Name("table_2");
        var location = new IcebergValues.Location("location");

        var createTableOneRq = new IcebergTable.CreateRequest(
                namespaceName,
                tableNameOne,
                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
                location,
                new IcebergTable.PartitionSpecification.Unpartitioned(),
                new IcebergTable.SortSpecification.Unsorted(),
                Map.of()
        );

        var createTableTwoRq = new IcebergTable.CreateRequest(
                namespaceName,
                tableNameTwo,
                new IcebergValues.Schema(IcebergCatalogCommons.DEFAULT_SCHEMA),
                location,
                new IcebergTable.PartitionSpecification.Unpartitioned(),
                new IcebergTable.SortSpecification.Unsorted(),
                Map.of()
        );

        var tableOne = new IcebergTable(namespaceName, tableNameOne);
        var tableTwo = new IcebergTable(namespaceName, tableNameTwo);
        var createdTableOne = catalog.createTable(createTableOneRq);
        var createdTableTwo = catalog.createTable(createTableTwoRq);

        var transaction = List.of(
                new IcebergTable.Transaction(
                        tableOne,
                        new IcebergTable.UpdateRequest(
                                List.of(
                                        new IcebergTable.UpdateRequest.Requirement.AssertTableUuid(createdTableOne.metadata().uuid())
                                ),
                                List.of(
                                        new IcebergTable.UpdateRequest.Update.SetPropertiesUpdate(
                                                Map.of("transaction-property", "table_one_property")
                                        )
                                )
                        )
                ),
                new IcebergTable.Transaction(
                        tableTwo,
                        new IcebergTable.UpdateRequest(
                                List.of(
                                        new IcebergTable.UpdateRequest.Requirement.AssertTableUuid(createdTableTwo.metadata().uuid())
                                ),
                                List.of(
                                        new IcebergTable.UpdateRequest.Update.SetPropertiesUpdate(
                                                Map.of("transaction-property", "table_two_property")
                                        )
                                )
                        )
                )
        );

        catalog.commitTransaction(transaction);

        var loadedTableOne = catalog.loadTable(tableOne);
        var loadedTableTwo = catalog.loadTable(tableTwo);

        assertEquals("table_one_property", loadedTableOne.metadata().properties().get("transaction-property"));
        assertEquals("table_two_property", loadedTableTwo.metadata().properties().get("transaction-property"));
    }
}
