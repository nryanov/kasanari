CREATE SCHEMA IF NOT EXISTS demo_lifecycle;

CREATE TABLE IF NOT EXISTS demo_lifecycle.events_lifecycle (
  id INTEGER,
  event_name VARCHAR,
  amount DOUBLE,
  source VARCHAR
);

INSERT INTO demo_lifecycle.events_lifecycle (id, event_name, amount, source) VALUES
  (1, 'signup', 10.0, 'seed'),
  (2, 'click', 20.0, 'seed'),
  (3, 'purchase', 30.0, 'seed');

SELECT id, event_name, amount, source
FROM demo_lifecycle.events_lifecycle
ORDER BY id;

UPDATE demo_lifecycle.events_lifecycle
SET source = 'updated'
WHERE id IN (1, 2);

SELECT id, event_name, amount, source
FROM demo_lifecycle.events_lifecycle
ORDER BY id;

CREATE OR REPLACE VIEW demo_lifecycle.events_lifecycle_v AS
SELECT id, event_name, amount
FROM demo_lifecycle.events_lifecycle
WHERE id <= 2;

SELECT id, event_name, amount
FROM demo_lifecycle.events_lifecycle_v
ORDER BY id;

ALTER TABLE demo_lifecycle.events_lifecycle
ADD COLUMN notes VARCHAR;

SELECT id, event_name, amount, source, notes
FROM demo_lifecycle.events_lifecycle
ORDER BY id;

DROP TABLE IF EXISTS demo_lifecycle.events_lifecycle;
DROP VIEW IF EXISTS demo_lifecycle.events_lifecycle_v;
