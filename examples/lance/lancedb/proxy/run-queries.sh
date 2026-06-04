#!/usr/bin/env sh
set -eu

BASE_URL="${BASE_URL:-http://localhost:9090}"
CATALOG_ID="${CATALOG_ID:-lance_proxy}"

echo "Registering ${CATALOG_ID}..."
curl -s -X POST "${BASE_URL}/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d "{
    \"catalogId\": \"${CATALOG_ID}\",
    \"catalogType\": \"LANCE\",
    \"mode\": \"PROXY\",
    \"spec\": {
      \"fileIoProperties\": {},
      \"catalogProperties\": {
        \"implementation\": \"dir\",
        \"root\": \"s3://warehouse\",
        \"manifest_enabled\": \"false\",
        \"dir_listing_enabled\": \"true\",
        \"storage.aws_access_key_id\": \"admin\",
        \"storage.aws_secret_access_key\": \"password\",
        \"storage.aws_endpoint\": \"http://minio:9000\",
        \"storage.aws_allow_http\": \"true\",
        \"storage.aws_virtual_hosted_style_request\": \"false\",
        \"storage.access_key_id\": \"admin\",
        \"storage.secret_access_key\": \"password\",
        \"storage.endpoint\": \"http://minio:9000\",
        \"storage.allow_http\": \"true\",
        \"storage.virtual_hosted_style_request\": \"false\",
        \"storage.region\": \"us-east-1\"
      }
    }
  }"
echo
echo "Fetching catalog metadata..."
curl -s "${BASE_URL}/management/v1/catalogs/LANCE/${CATALOG_ID}"
echo
