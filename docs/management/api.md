# Management API

The Management API controls runtime catalog registration and RBAC role bindings.

Base path:

- `/management/v1`

Primary OpenAPI source:

- `spec/management/kasanari-management-catalog-service.yaml`

## Authentication and authorization prerequisites

Security depends on active providers:

- Authentication provider (`kasanari.authentication.type`) validates caller identity.
- Authorization provider (`kasanari.authorization.type`) enforces permissions.

Common outcomes:

- `401 Unauthorized`: caller is not authenticated.
- `403 Forbidden`: caller is authenticated but lacks required permission.

## Catalog lifecycle

Supported operations:

- `POST /management/v1/catalogs` - create catalog metadata
- `GET /management/v1/catalogs/{catalogType}/{catalogId}` - get catalog metadata
- `PATCH /management/v1/catalogs/{catalogType}/{catalogId}` - update metadata
- `DELETE /management/v1/catalogs/{catalogType}/{catalogId}` - delete metadata

`catalogType` values:

- `ICEBERG`
- `PAIMON`
- `LANCE`

`mode` values:

- `INTERNAL`
- `PROXY`

`spec` object is split into:

- `fileIoProperties`: storage/filesystem connector settings
- `catalogProperties`: catalog implementation settings

## Create catalog examples

### Iceberg internal

```bash
curl -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId":"iceberg-internal",
    "catalogType":"ICEBERG",
    "mode":"INTERNAL",
    "spec":{
      "fileIoProperties":{},
      "catalogProperties":{
        "uri":"jdbc:postgresql://localhost:5432/postgres",
        "kasanari.jdbc.user":"postgres",
        "kasanari.jdbc.password":"postgres",
        "warehouse":"s3a://warehouse",
        "io-impl":"org.apache.iceberg.aws.s3.S3FileIO"
      }
    }
  }'
```

Expected response:

- HTTP `201 Created`
- Body contains `catalogId`, `catalogType`, `mode`, `spec`, and `version`

### Paimon proxy

```json
{
  "catalogId": "paimon-proxy",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {
      "fs.s3a.endpoint": "http://localhost:9000"
    },
    "catalogProperties": {
      "type": "filesystem",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

### Lance proxy

```json
{
  "catalogId": "lance-proxy",
  "catalogType": "LANCE",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "implementation": "dir",
      "root": "s3://warehouse"
    }
  }
}
```

## Update and delete catalog

Update (optimistic concurrency is optional):

- `PATCH /management/v1/catalogs/{catalogType}/{catalogId}`
- Request body:
  - `spec` (required)
  - `expectedVersion` (optional)

Delete:

- `DELETE /management/v1/catalogs/{catalogType}/{catalogId}`
- Returns `204` on success, `404` if not found.

## Role bindings API

Supported operations:

- `GET /management/v1/security/roles` - list bindings (`subject`, `catalogType` optional filters)
- `PUT /management/v1/security/roles` - upsert bindings
- `DELETE /management/v1/security/roles` - delete bindings

Role binding shape:

```json
{
  "subject": "alice",
  "catalogType": "ICEBERG",
  "role": "IcebergCatalogViewer"
}
```

Current model does not include a separate `domain` field; scope is expressed by `catalogType`.

## What role binding kinds exist

By scope key:

- Subject + `ICEBERG` + role
- Subject + `PAIMON` + role
- Subject + `LANCE` + role

By role level (Casbin default roles):

- `*CatalogAdmin`
- `*CatalogEditor`
- `*CatalogViewer`

Examples:

- `IcebergCatalogAdmin`, `IcebergCatalogEditor`, `IcebergCatalogViewer`
- `PaimonCatalogAdmin`, `PaimonCatalogEditor`, `PaimonCatalogViewer`
- `LanceCatalogAdmin`, `LanceCatalogEditor`, `LanceCatalogViewer`

`*CatalogAdmin` roles include role-management permissions (`RoleSelect`, `RoleAdd`, `RoleRemove`) for the same catalog type.

## Create or update role bindings

### Request

```bash
curl -X PUT "http://localhost:9090/management/v1/security/roles" \
  -H "Content-Type: application/json" \
  -d '{
    "bindings":[
      {"subject":"platform-admin","catalogType":"ICEBERG","role":"IcebergCatalogAdmin"},
      {"subject":"team-a","catalogType":"PAIMON","role":"PaimonCatalogEditor"},
      {"subject":"analyst","catalogType":"LANCE","role":"LanceCatalogViewer"}
    ]
  }'
```

### Response

- HTTP `200 OK`
- Body:

```json
{
  "bindings": [
    {"subject":"platform-admin","catalogType":"ICEBERG","role":"IcebergCatalogAdmin"},
    {"subject":"team-a","catalogType":"PAIMON","role":"PaimonCatalogEditor"},
    {"subject":"analyst","catalogType":"LANCE","role":"LanceCatalogViewer"}
  ]
}
```

## Delete role bindings

```bash
curl -X DELETE "http://localhost:9090/management/v1/security/roles" \
  -H "Content-Type: application/json" \
  -d '{
    "bindings":[
      {"subject":"analyst","catalogType":"LANCE","role":"LanceCatalogViewer"}
    ]
  }'
```

Expected response:

- HTTP `204 No Content`

## Typical RBAC scenarios

- Platform team grants `IcebergCatalogAdmin` to an operations group for full Iceberg management.
- Data engineering team gets `PaimonCatalogEditor` for write workflows without role administration.
- Analysts get `LanceCatalogViewer` for read-only access to Lance resources.
- CI/CD service account receives only one `*CatalogEditor` role for the engine it deploys.

## Troubleshooting

- `401` on management endpoints:
  - Verify authentication provider config and credentials/token.
- `403` on catalog operations:
  - Verify role bindings map to correct `catalogType`.
  - Verify role name exists for active authorization provider.
- Catalog not visible immediately after updates:
  - Routers refresh periodically (`kasanari.catalog.refresh-interval`, default `30s`).
- Role changes not taking effect:
  - Roles API triggers policy reload after upsert/delete; verify provider is `casbin` when expecting persisted RBAC.
