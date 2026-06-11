[[_TOC_]]

# Kasanari (重なり)

Goal: implement REST catalogs for `Apache Iceberg` and `Apache Paimon` 
which may act like a complete catalog implementation or just a proxy to other (like `hive`, `jdbc` or even another `rest` catalog implementation).

## Examples
- Catalog usage examples: [examples/README.md](examples/README.md)
- Includes Iceberg, Paimon, Lance, and auth/authz-focused local stacks.

## Testing
### Docker issues
- Colima: `sudo ln -sf $HOME/.colima/default/docker.sock /var/run/docker.sock`

### Catalog tests
Some catalog tests currently expects running postgres instance:
```shell
docker run -p 5432:5432 postgres:17 -d
```

or yugabyte:
```shell
docker run -d --name yugabyte -p 7000:7000 -p 9000:9000 -p 15433:15433 -p 5433:5433 -p 9042:9042 \
 yugabytedb/yugabyte:2.25.2.0-b359 bin/yugabyted start \
 --background=false
```
