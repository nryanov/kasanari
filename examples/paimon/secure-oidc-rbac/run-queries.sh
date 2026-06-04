#!/usr/bin/env sh
set -eu

BASE_URL="${BASE_URL:-http://localhost:9090}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
CATALOG_ID="${CATALOG_ID:-paimon_secure_oidc}"

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
    \"catalogType\": \"PAIMON\",
    \"mode\": \"INTERNAL\",
    \"spec\": {
      \"fileIoProperties\": {
        \"fs.s3a.access.key\": \"admin\",
        \"fs.s3a.secret.key\": \"password\",
        \"fs.s3a.impl\": \"org.apache.hadoop.fs.s3a.S3AFileSystem\",
        \"fs.s3a.path.style.access\": \"true\",
        \"fs.s3a.endpoint\": \"http://minio:9000\"
      },
      \"catalogProperties\": {
        \"warehouse\": \"s3a://warehouse\",
        \"uri\": \"jdbc:postgresql://catalog-storage:5432/postgres\",
        \"kasanari.jdbc.user\": \"postgres\",
        \"kasanari.jdbc.password\": \"postgres\",
        \"kasanari.catalog.key\": \"${CATALOG_ID}\"
      }
    }
  }"
echo
echo "Fetching registered catalog..."
curl -s -H "Authorization: Bearer ${TOKEN}" "${BASE_URL}/management/v1/catalogs/PAIMON/${CATALOG_ID}"
echo
