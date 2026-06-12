# Integrations

Kasanari is designed as an API-first catalog layer for multi-engine interoperability.

This project currently provides runnable integration examples for:

- Trino
- Spark (Jupyter notebooks)
- StarRocks
- LanceDB (Python client)

## Integration guides

| Engine / client | Catalogs in examples | Entry point |
|---|---|---|
| Trino | Iceberg (`INTERNAL`) | `examples/trino/README.md` |
| Spark | Iceberg (`INTERNAL` + `PROXY`), Paimon (`INTERNAL`), Lance (`INTERNAL`) | `examples/spark/README.md` |
| StarRocks | Iceberg (`INTERNAL`) | `examples/starrocks/README.md` |
| LanceDB | Lance (`INTERNAL` + `PROXY`) | `examples/lance/lancedb/internal/README.md`, `examples/lance/lancedb/proxy/README.md` |

## Recommended path

1. Complete `quickstart.md` (baseline services + Iceberg registration).
2. Pick an engine guide from `examples/`.
3. Register a catalog through `POST /management/v1/catalogs`.
4. Execute the sample query/workflow from that engine example.

## Notes

- API coverage differs by catalog type; review each catalog page before selecting an engine workflow.
- Security settings apply consistently across integrations because all requests pass through Kasanari auth/authz layers.
