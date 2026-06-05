CREATE EXTERNAL CATALOG IF NOT EXISTS iceberg_starrocks_internal
PROPERTIES (
  "type" = "iceberg",
  "iceberg.catalog.type" = "rest",
  "iceberg.catalog.uri" = "http://kasanari:9090/iceberg",
  "iceberg.catalog.warehouse" = "iceberg_starrocks_internal",
  "catalog.access.control" = "allowall",
  "aws.s3.enable_ssl" = "false",
  "aws.s3.enable_path_style_access" = "true",
  "aws.s3.endpoint" = "http://minio:9000",
  "aws.s3.access_key" = "admin",
  "aws.s3.secret_key" = "password"
);

SET CATALOG iceberg_starrocks_internal;

CREATE DATABASE IF NOT EXISTS demo_lifecycle;

CREATE TABLE IF NOT EXISTS demo_lifecycle.events_lifecycle (
  id INT,
  event_name STRING,
  amount DOUBLE,
  source STRING
);

INSERT INTO demo_lifecycle.events_lifecycle (id, event_name, amount, source) VALUES
  (1, 'signup', 10.0, 'seed'),
  (2, 'click', 20.0, 'seed'),
  (3, 'purchase', 30.0, 'seed');

SELECT id, event_name, amount, source
FROM demo_lifecycle.events_lifecycle
ORDER BY id;

-- StarRocks 4.1 does not support UPDATE on Iceberg tables; use DELETE + INSERT instead.
DELETE FROM demo_lifecycle.events_lifecycle
WHERE id IN (1, 2);

INSERT INTO demo_lifecycle.events_lifecycle (id, event_name, amount, source) VALUES
  (1, 'signup', 10.0, 'updated'),
  (2, 'click', 20.0, 'updated');

SELECT id, event_name, amount, source
FROM demo_lifecycle.events_lifecycle
ORDER BY id;

ALTER TABLE demo_lifecycle.events_lifecycle
ADD COLUMN notes STRING;

SELECT id, event_name, amount, source, notes
FROM demo_lifecycle.events_lifecycle
ORDER BY id;

CREATE VIEW demo_lifecycle.events_lifecycle_v AS
SELECT id, event_name, amount
FROM demo_lifecycle.events_lifecycle
WHERE id <= 2;

SELECT id, event_name, amount
FROM demo_lifecycle.events_lifecycle_v
ORDER BY id;

DELETE FROM demo_lifecycle.events_lifecycle
WHERE id = 3;

SELECT id, event_name, amount, source, notes
FROM demo_lifecycle.events_lifecycle
ORDER BY id;

DROP VIEW IF EXISTS demo_lifecycle.events_lifecycle_v;
DROP TABLE IF EXISTS demo_lifecycle.events_lifecycle;
