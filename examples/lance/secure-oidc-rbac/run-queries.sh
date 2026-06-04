#!/usr/bin/env sh
set -eu

BASE_URL="${BASE_URL:-http://localhost:9090}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
CATALOG_ID="${CATALOG_ID:-lance_secure_oidc}"

TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/kasanari/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=kasanari-api" \
  -d "client_secret=kasanari-api-secret" \
  -d "username=admin" \
  -d "password=admin" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

if [ -z "${TOKEN}" ]; then
  echo "Failed to obtain OIDC token"
  exit 1
fi

echo "Registering ${CATALOG_ID}..."
curl -s -X POST "${BASE_URL}/management/v1/catalogs" \
  -H "Authorization: Bearer ${TOKEN}" \
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
echo "Fetching registered catalog..."
curl -s -H "Authorization: Bearer ${TOKEN}" "${BASE_URL}/management/v1/catalogs/LANCE/${CATALOG_ID}"
echo
