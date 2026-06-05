CREATE DATABASE IF NOT EXISTS demo_lifecycle;
USE demo_lifecycle;

CREATE TABLE IF NOT EXISTS events_lifecycle (
  id INT,
  event_name STRING,
  amount DOUBLE,
  source STRING
)
DUPLICATE KEY(id)
DISTRIBUTED BY HASH(id) BUCKETS 1
PROPERTIES("replication_num" = "1");

INSERT INTO events_lifecycle (id, event_name, amount, source) VALUES
  (1, 'signup', 10.0, 'seed'),
  (2, 'click', 20.0, 'seed'),
  (3, 'purchase', 30.0, 'seed');

SELECT id, event_name, amount, source
FROM events_lifecycle
ORDER BY id;

UPDATE events_lifecycle
SET source = 'updated'
WHERE id IN (1, 2);

SELECT id, event_name, amount, source
FROM events_lifecycle
ORDER BY id;

CREATE OR REPLACE VIEW events_lifecycle_v AS
SELECT id, event_name, amount
FROM events_lifecycle
WHERE id <= 2;

SELECT id, event_name, amount
FROM events_lifecycle_v
ORDER BY id;

ALTER TABLE events_lifecycle
ADD COLUMN notes STRING NULL;

SELECT id, event_name, amount, source, notes
FROM events_lifecycle
ORDER BY id;

DROP TABLE IF EXISTS events_lifecycle;
DROP VIEW IF EXISTS events_lifecycle_v;
