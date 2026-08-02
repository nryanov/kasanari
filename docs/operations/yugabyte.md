# YugabyteDB backend

Kasanari can persist management metadata, Casbin role bindings, and INTERNAL catalog metadata in [YugabyteDB](https://www.yugabyte.com/) (YSQL) instead of PostgreSQL.

Selection is **explicit** via:

```properties
kasanari.repository.backend=yugabyte
```

Unset or `postgres` keeps the PostgreSQL repository modules.

## Minimum version

Use **YugabyteDB 2025.1+**. Transactional advisory locks (`pg_advisory_xact_lock`) are required for Paimon INTERNAL catalogs.

## JDBC

Prefer the Yugabyte JDBC smart driver:

```text
jdbc:yugabytedb://yb-tserver:5433/kasanari?load-balance=true
```

Credentials use the same keys as PostgreSQL:

| Key | Purpose |
|-----|---------|
| `uri` | JDBC URL |
| `kasanari.jdbc.user` | user |
| `kasanari.jdbc.password` | password |
| `kasanari.repository.backend` | `yugabyte` |

### Management

```properties
kasanari.management.metadata.jdbc-properties."uri"=jdbc:yugabytedb://yb-tserver:5433/kasanari?load-balance=true
kasanari.management.metadata.jdbc-properties."kasanari.jdbc.user"=yugabyte
kasanari.management.metadata.jdbc-properties."kasanari.jdbc.password"=yugabyte
kasanari.management.metadata.jdbc-properties."kasanari.repository.backend"=yugabyte
```

### Casbin

```properties
kasanari.authorization.casbin.jdbc.uri=jdbc:yugabytedb://yb-tserver:5433/kasanari?load-balance=true
kasanari.authorization.casbin.jdbc.user=yugabyte
kasanari.authorization.casbin.jdbc.password=yugabyte
kasanari.authorization.casbin.repository.backend=yugabyte
```

### INTERNAL catalogs

Set the same properties inside each catalog’s `catalogProperties` when registering via the Management API. For Lance INTERNAL, Kasanari also injects `kasanari.catalog.key=<management catalog id>` so rows are isolated per catalog.

## Database layout

Create the application database as **colocated** (small global tables):

```sql
CREATE DATABASE kasanari WITH COLOCATION = true;
```

| Plane | Distribution |
|-------|----------------|
| `kasanari_catalogs`, Casbin bindings | Colocated (`WITH (colocation = true)`) — list/refresh across catalogs |
| Iceberg / Paimon / Lance INTERNAL | Hash-sharded on `catalog_key`, `WITH (colocation = false)` — all rows for one catalog share a tablet |

Cross-catalog distributed transactions are not used. Catalog operations always target a single `catalog_key`. The only cross-catalog read is Casbin listing bindings for a role (colocated tables).

## Naming: `catalog_key`

Yugabyte modules use a unified `catalog_key` column for Iceberg, Paimon, and Lance.

Postgres Lance historically had no tenant column (oversight from the first draft). The Yugabyte Lance schema includes `catalog_key`; Postgres Lance is unchanged.

## No migration tooling

This is a greenfield choice of backend. There is no automated Postgres ↔ Yugabyte data migration.
