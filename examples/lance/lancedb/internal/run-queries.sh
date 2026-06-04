#!/usr/bin/env sh
set -eu

BASE_URL="${BASE_URL:-http://localhost:9090}"
CATALOG_ID="${CATALOG_ID:-lance_internal}"

echo "Registering ${CATALOG_ID}..."
curl -s -X POST "${BASE_URL}/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d "{
    \"catalogId\": \"${CATALOG_ID}\",
    \"catalogType\": \"LANCE\",
    \"mode\": \"INTERNAL\",
    \"spec\": {
      \"fileIoProperties\": {},
      \"catalogProperties\": {
        \"implementation\": \"dir\",
        \"root\": \"s3://warehouse\",
        \"lance.warehouse.location\": \"s3://warehouse\",
        \"kasanari.jdbc.user\": \"postgres\",
        \"kasanari.jdbc.password\": \"postgres\",
        \"uri\": \"jdbc:postgresql://catalog-storage:5432/postgres\",
        \"lance.storage.storage_options.aws_access_key_id\": \"admin\",
        \"lance.storage.storage_options.aws_secret_access_key\": \"password\",
        \"lance.storage.storage_options.aws_endpoint\": \"http://minio:9000\",
        \"lance.storage.storage_options.aws_allow_http\": \"true\",
        \"lance.storage.storage_options.aws_virtual_hosted_style_request\": \"false\",
        \"lance.storage.storage_options.region\": \"us-east-1\"
      }
    }
  }"
echo
echo "Fetching catalog metadata..."
curl -s "${BASE_URL}/management/v1/catalogs/LANCE/${CATALOG_ID}"
echo
