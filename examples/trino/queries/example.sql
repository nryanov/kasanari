CREATE CATALOG IF NOT EXISTS iceberg_trino_internal USING iceberg
WITH (
  "iceberg.catalog.type" = 'rest',
  "iceberg.rest-catalog.uri" = 'http://kasanari:9090/iceberg',
  "iceberg.rest-catalog.warehouse" = 'iceberg_trino_internal',
  "fs.native-s3.enabled" = 'true',
  "s3.endpoint" = 'http://minio:9000',
  "s3.aws-access-key" = 'admin',
  "s3.aws-secret-key" = 'password',
  "s3.path-style-access" = 'true',
  "s3.region" = 'us-east-1'
);

CREATE SCHEMA IF NOT EXISTS iceberg_trino_internal.demo_lifecycle;

CREATE TABLE IF NOT EXISTS iceberg_trino_internal.demo_lifecycle.events_lifecycle (
  id INTEGER,
  event_name VARCHAR,
  amount DOUBLE,
  source VARCHAR
);

INSERT INTO iceberg_trino_internal.demo_lifecycle.events_lifecycle (id, event_name, amount, source) VALUES
  (1, 'signup', 10.0, 'seed'),
  (2, 'click', 20.0, 'seed'),
  (3, 'purchase', 30.0, 'seed');

SELECT id, event_name, amount, source
FROM iceberg_trino_internal.demo_lifecycle.events_lifecycle
ORDER BY id;

UPDATE iceberg_trino_internal.demo_lifecycle.events_lifecycle
SET source = 'updated'
WHERE id IN (1, 2);

SELECT id, event_name, amount, source
FROM iceberg_trino_internal.demo_lifecycle.events_lifecycle
ORDER BY id;

ALTER TABLE iceberg_trino_internal.demo_lifecycle.events_lifecycle
ADD COLUMN notes VARCHAR;

SELECT id, event_name, amount, source, notes
FROM iceberg_trino_internal.demo_lifecycle.events_lifecycle
ORDER BY id;

CREATE OR REPLACE VIEW iceberg_trino_internal.demo_lifecycle.events_lifecycle_v AS
SELECT id, event_name, amount
FROM iceberg_trino_internal.demo_lifecycle.events_lifecycle
WHERE id <= 2;

SELECT id, event_name, amount
FROM iceberg_trino_internal.demo_lifecycle.events_lifecycle_v
ORDER BY id;

DELETE FROM iceberg_trino_internal.demo_lifecycle.events_lifecycle
WHERE id = 3;

SELECT id, event_name, amount, source, notes
FROM iceberg_trino_internal.demo_lifecycle.events_lifecycle
ORDER BY id;

DROP VIEW IF EXISTS iceberg_trino_internal.demo_lifecycle.events_lifecycle_v;
DROP TABLE IF EXISTS iceberg_trino_internal.demo_lifecycle.events_lifecycle;
