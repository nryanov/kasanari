# Examples

This page maps runnable examples from the `examples/` directory.

Build Kasanari image once before running containerized examples:

```bash
./scripts/build-container-images.sh
```

## Catalog examples

| Example path | Catalog/mode | What it demonstrates |
|---|---|---|
| `examples/trino` | Iceberg `INTERNAL` | Trino query engine against Iceberg REST via Kasanari |
| `examples/starrocks` | Iceberg `INTERNAL` | StarRocks integration with Iceberg REST via Kasanari |
| `examples/spark` | Iceberg `INTERNAL` + Iceberg `PROXY` + Paimon `INTERNAL` + Lance `INTERNAL` | Notebook-driven integration and API registration |
| `examples/lance/lancedb/internal` | Lance `INTERNAL` | Lance namespace/table metadata with LanceDB client |
| `examples/lance/lancedb/proxy` | Lance `PROXY` (`implementation=dir`) | Lance metadata proxy flow with LanceDB client |

## Authentication examples

| Example path | Provider | Notes |
|---|---|---|
| `examples/authentication/none` | `none` | No authentication required |
| `examples/authentication/ldap` | `ldap` | HTTP Basic against LDAP |
| `examples/authentication/oidc` | `oidc` | JWT Bearer validation via OIDC issuer |
| `examples/authentication/custom` | custom SPI | Header-token auth provider example |

## Authorization examples

| Example path | Provider | Notes |
|---|---|---|
| `examples/authorization/custom` | custom SPI | Subject allow-list authorization example |

## Recommended starting points

- Fastest end-to-end baseline: `quickstart.md`
- SQL workflow with query engine: `examples/trino/README.md`
- Security-focused setups: `examples/authentication/ldap/README.md`, `examples/authentication/oidc/README.md`
- Integration-oriented overview: `integrations/index.md`
