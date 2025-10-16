package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.DeprecatedIcebergCatalogAdapterTest;
import kasanari.catalog.iceberg.core.IcebergCatalogCommons;
import kasanari.catalog.iceberg.kasanari.stub.repository.jdbc.JdbcTableRepositoryStub;
import org.apache.iceberg.MetadataUpdate;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.UpdateRequirement;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KasanariIcebergCatalogTest extends DeprecatedIcebergCatalogAdapterTest {
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
        var namespace = Namespace.of("ns_multi_table_tx1");
        var tableOne = TableIdentifier.of(namespace, "table_1");
        var tableTwo = TableIdentifier.of(namespace, "table_2");

        var createTableOneRq = CreateTableRequest
                .builder()
                .withName("table_1")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createTableTwoRq = CreateTableRequest
                .builder()
                .withName("table_2")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        catalog.createNamespace(namespace);

        var createdTableOne = catalog.createTable(namespace, createTableOneRq);
        var createdTableTwo = catalog.createTable(namespace, createTableTwoRq);

        var transaction = List.of(
                UpdateTableRequest
                        .create(tableOne,
                                List.of(new UpdateRequirement.AssertTableUUID(createdTableOne.tableMetadata().uuid())),
                                List.of(new MetadataUpdate.SetProperties(Map.of("transaction-property", "table_one_property")))
                        ),
                UpdateTableRequest
                        .create(tableTwo,
                                List.of(new UpdateRequirement.AssertTableUUID(createdTableTwo.tableMetadata().uuid())),
                                List.of(new MetadataUpdate.SetProperties(Map.of("transaction-property", "table_two_property")))
                        )
        );

        catalog.commitTransaction(transaction);

        var loadedTableOne = catalog.loadTable(tableOne);
        var loadedTableTwo = catalog.loadTable(tableTwo);

        assertEquals("table_one_property", loadedTableOne.tableMetadata().properties().get("transaction-property"));
        assertEquals("table_two_property", loadedTableTwo.tableMetadata().properties().get("transaction-property"));
    }

    @Test
    public void correctlyRollbackFailedMultiTableTransaction() {
        var kasanariCatalog = (KasanariCatalog) catalog.delegate();
        var tableRepository = kasanariCatalog.getTableRepository();
        kasanariCatalog.setTableRepository(new JdbcTableRepositoryStub(tableRepository) {
            @Override
            public boolean update(Handle tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
                // fail no commit changes to second table
                if (tableIdentifier.name().equals("table_2")) {
                    // to avoid commit retries
                    throw new RuntimeException("Intentional commit failure");
                }

                return super.update(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation);
            }
        });

        var namespace = Namespace.of("ns_multi_table_tx2");
        var tableOne = TableIdentifier.of(namespace, "table_1");
        var tableTwo = TableIdentifier.of(namespace, "table_2");

        var createTableOneRq = CreateTableRequest
                .builder()
                .withName("table_1")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createTableTwoRq = CreateTableRequest
                .builder()
                .withName("table_2")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        catalog.createNamespace(namespace);

        var createdTableOne = catalog.createTable(namespace, createTableOneRq);
        var createdTableTwo = catalog.createTable(namespace, createTableTwoRq);

        var transaction = List.of(
                UpdateTableRequest
                        .create(tableOne,
                                List.of(new UpdateRequirement.AssertTableUUID(createdTableOne.tableMetadata().uuid())),
                                List.of(new MetadataUpdate.SetProperties(Map.of("transaction-property", "table_one_property")))
                        ),
                UpdateTableRequest
                        .create(tableTwo,
                                List.of(new UpdateRequirement.AssertTableUUID(createdTableTwo.tableMetadata().uuid())),
                                List.of(new MetadataUpdate.SetProperties(Map.of("transaction-property", "table_two_property")))
                        )
        );

        try {
            catalog.commitTransaction(transaction);
        } catch (Exception e) {
            // ignore error
        }

        var loadedTableOne = catalog.loadTable(tableOne);
        var loadedTableTwo = catalog.loadTable(tableTwo);

        assertNull(loadedTableOne.tableMetadata().properties().get("transaction-property"));
        assertNull(loadedTableTwo.tableMetadata().properties().get("transaction-property"));
    }

    @Test
    public void doNotRollbackAllChangesInMultiTableTransactionUsingDefaultCatalogImplementation() {
        var defaultCatalog = new KasanariIcebergCatalogAdapter((KasanariCatalog) catalog.delegate(), false);
        var kasanariCatalog = (KasanariCatalog) defaultCatalog.delegate();
        var tableRepository = kasanariCatalog.getTableRepository();
        kasanariCatalog.setTableRepository(new JdbcTableRepositoryStub(tableRepository) {
            @Override
            public boolean update(TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
                // fail no commit changes to second table
                if (tableIdentifier.name().equals("table_2")) {
                    // to avoid commit retries
                    throw new RuntimeException("Intentional commit failure");
                }

                return super.update(tableIdentifier, previousMetadataLocation, newMetadataLocation);
            }
        });

        var namespace = Namespace.of("ns_multi_table_tx3");
        var tableOne = TableIdentifier.of(namespace, "table_1");
        var tableTwo = TableIdentifier.of(namespace, "table_2");

        var createTableOneRq = CreateTableRequest
                .builder()
                .withName("table_1")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createTableTwoRq = CreateTableRequest
                .builder()
                .withName("table_2")
                .withLocation("location")
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        kasanariCatalog.createNamespace(namespace);

        var createdTableOne = defaultCatalog.createTable(namespace, createTableOneRq);
        var createdTableTwo = defaultCatalog.createTable(namespace, createTableTwoRq);

        var transaction = List.of(
                UpdateTableRequest
                        .create(tableOne,
                                List.of(new UpdateRequirement.AssertTableUUID(createdTableOne.tableMetadata().uuid())),
                                List.of(new MetadataUpdate.SetProperties(Map.of("transaction-property", "table_one_property")))
                        ),
                UpdateTableRequest
                        .create(tableTwo,
                                List.of(new UpdateRequirement.AssertTableUUID(createdTableTwo.tableMetadata().uuid())),
                                List.of(new MetadataUpdate.SetProperties(Map.of("transaction-property", "table_two_property")))
                        )
        );

        try {
            defaultCatalog.commitTransaction(transaction);
        } catch (Exception e) {
            // ignore error
        }

        var loadedTableOne = defaultCatalog.loadTable(tableOne);
        var loadedTableTwo = defaultCatalog.loadTable(tableTwo);

        // first table changes were committed
        assertEquals("table_one_property", loadedTableOne.tableMetadata().properties().get("transaction-property"));
        // second table changes were rolled back due to it as executed in a different transactions (default implementation)
        assertNull(loadedTableTwo.tableMetadata().properties().get("transaction-property"));
    }
}
