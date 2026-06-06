-- ClickHouse Iceberg REST catalog lifecycle (Kasanari internal mode).
-- Requires ClickHouse 25.7+ for Iceberg writes and 24.12+ for REST catalog reads.

SET allow_database_iceberg = 1;
SET allow_insert_into_iceberg = 1;

-- Connector creation (Iceberg REST catalog via DataLakeCatalog database engine).
CREATE DATABASE IF NOT EXISTS iceberg_clickhouse_internal
ENGINE = DataLakeCatalog('http://kasanari:9090/iceberg')
SETTINGS
    catalog_type = 'rest',
    storage_endpoint = 'http://minio:9000/warehouse',
    warehouse = 'iceberg_clickhouse_internal',
    aws_access_key_id = 'admin',
    aws_secret_access_key = 'password',
    region = 'us-east-1';

-- Namespace create: not supported as standalone SQL in ClickHouse.
-- Workaround: the namespace is created automatically when the first table is registered in it.

CREATE TABLE iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
(
    id Nullable(Int32),
    event_name Nullable(String),
    amount Nullable(Float64),
    source Nullable(String)
) ENGINE = Iceberg('http://minio:9000/warehouse/demo_lifecycle/events_lifecycle/', 'admin', 'password');

INSERT INTO iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle` (id, event_name, amount, source) VALUES
    (1, 'signup', 10.0, 'seed'),
    (2, 'click', 20.0, 'seed'),
    (3, 'purchase', 30.0, 'seed');

SELECT id, event_name, amount, source
FROM iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
ORDER BY id;

-- UPDATE: not supported on Iceberg tables; use DELETE + INSERT instead.
ALTER TABLE iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
DELETE WHERE id IN (1, 2);

INSERT INTO iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle` (id, event_name, amount, source) VALUES
    (1, 'signup', 10.0, 'updated'),
    (2, 'click', 20.0, 'updated');

SELECT id, event_name, amount, source
FROM iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
ORDER BY id;

ALTER TABLE iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
ADD COLUMN notes Nullable(String);

SELECT id, event_name, amount, source, notes
FROM iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
ORDER BY id;

-- CREATE VIEW: Iceberg catalog views are not supported; use a local ClickHouse view instead.
CREATE DATABASE IF NOT EXISTS demo_lifecycle;

CREATE OR REPLACE VIEW demo_lifecycle.events_lifecycle_v AS
SELECT id, event_name, amount
FROM iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
WHERE id <= 2;

SELECT id, event_name, amount
FROM demo_lifecycle.events_lifecycle_v
ORDER BY id;

ALTER TABLE iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
DELETE WHERE id = 3;

SELECT id, event_name, amount, source, notes
FROM iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`
ORDER BY id;

DROP TABLE IF EXISTS iceberg_clickhouse_internal.`demo_lifecycle.events_lifecycle`;
DROP VIEW IF EXISTS demo_lifecycle.events_lifecycle_v;
