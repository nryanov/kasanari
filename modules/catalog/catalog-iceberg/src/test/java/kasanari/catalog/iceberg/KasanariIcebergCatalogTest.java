package kasanari.catalog.iceberg;

import kasanari.catalog.iceberg.stub.JdbcTableRepositoryStub;
import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.catalog.iceberg.s3.NoneRegionS3FileIOAwsClientFactory;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.MetadataUpdate;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.UpdateRequirement;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Timeout(value = 5, unit = TimeUnit.SECONDS)
public class KasanariIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;
    private PostgresHelper postgresHelper;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        postgres.start();
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        postgresHelper = new PostgresHelper(postgres);

        var factory = new KasanariIcebergCatalogFactory();
        var properties = new HashMap<String, String>();
        properties.put(KasanariDataSourceConfiguration.USER, postgres.username());
        properties.put(KasanariDataSourceConfiguration.PASSWORD, postgres.password());
        properties.put(KasanariDataSourceConfiguration.URI, postgres.jdbcUrl());
        properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse");
        properties.put(S3FileIOProperties.ENDPOINT, s3Container.url());
        properties.put(S3FileIOProperties.ACCESS_KEY_ID, s3Container.username());
        properties.put(S3FileIOProperties.SECRET_ACCESS_KEY, s3Container.password());
        properties.put(S3FileIOProperties.PATH_STYLE_ACCESS, "true");
        properties.put(S3FileIOProperties.CLIENT_FACTORY, NoneRegionS3FileIOAwsClientFactory.class.getName());

        return factory.create("kasanari", Map.of(), properties);
    }

    @Override
    public void close() {
        postgres.stop();
        s3Container.stop();
    }

    @Override
    public void reset() {
        postgresHelper.truncateTable("kasanari_iceberg_namespace_properties");
        postgresHelper.truncateTable("kasanari_iceberg_tables");
        postgresHelper.truncateTable("kasanari_iceberg_views");
        postgresHelper.truncateTable("kasanari_iceberg_namespaces");
        s3Helper.clearBucket("warehouse");
    }

    @Override
    public String entityLocation(TableIdentifier identifier) {
        return "s3a://warehouse/" + identifier.namespace() + "/" + identifier.name();
    }

    @Test
    public void successfullyCommitMultiTableTransaction() {
        var namespace = namespaceName();
        var tableOneName = uniqueTableName();
        var tableTwoName = uniqueTableName();
        var tableOne = TableIdentifier.of(namespace, tableOneName);
        var tableTwo = TableIdentifier.of(namespace, tableTwoName);

        var createTableOneRq = CreateTableRequest
                .builder()
                .withName(tableOneName)
                .withLocation(entityLocation(tableOne))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createTableTwoRq = CreateTableRequest
                .builder()
                .withName(tableTwoName)
                .withLocation(entityLocation(tableTwo))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

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
        var namespace = namespaceName();
        var tableOneName = uniqueTableName();
        var tableTwoName = uniqueTableName();

        var kasanariCatalog = (KasanariIcebergCatalog) catalog.delegate();
        var tableRepository = kasanariCatalog.getTableRepository();
        kasanariCatalog.setTableRepository(new JdbcTableRepositoryStub(tableRepository) {
            @Override
            public boolean update(Handle tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
                // fail no commit changes to second table
                if (tableIdentifier.name().equals(tableTwoName)) {
                    // to avoid commit retries
                    throw new RuntimeException("Intentional commit failure");
                }

                return super.update(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation);
            }
        });
        var tableOne = TableIdentifier.of(namespace, tableOneName);
        var tableTwo = TableIdentifier.of(namespace, tableTwoName);

        var createTableOneRq = CreateTableRequest
                .builder()
                .withName(tableOneName)
                .withLocation(entityLocation(tableOne))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createTableTwoRq = CreateTableRequest
                .builder()
                .withName(tableTwoName)
                .withLocation(entityLocation(tableTwo))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

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
        var namespace = namespaceName();
        var tableOneName = uniqueTableName();
        var tableTwoName = uniqueTableName();
        var tableOne = TableIdentifier.of(namespace, tableOneName);
        var tableTwo = TableIdentifier.of(namespace, tableTwoName);

        var defaultCatalog = new KasanariIcebergCatalogAdapter((KasanariIcebergCatalog) catalog.delegate(), false);
        var kasanariCatalog = (KasanariIcebergCatalog) defaultCatalog.delegate();
        var tableRepository = kasanariCatalog.getTableRepository();
        kasanariCatalog.setTableRepository(new JdbcTableRepositoryStub(tableRepository) {
            @Override
            public boolean update(Handle tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
                // fail no commit changes to second table
                if (tableIdentifier.name().equals(tableTwoName)) {
                    // to avoid commit retries
                    throw new RuntimeException("Intentional commit failure");
                }

                return super.update(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation);
            }
        });

        var createTableOneRq = CreateTableRequest
                .builder()
                .withName(tableOneName)
                .withLocation(entityLocation(tableOne))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

        var createTableTwoRq = CreateTableRequest
                .builder()
                .withName(tableTwoName)
                .withLocation(entityLocation(tableTwo))
                .withSchema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .withWriteOrder(SortOrder.unsorted())
                .withPartitionSpec(PartitionSpec.unpartitioned())
                .build();

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
