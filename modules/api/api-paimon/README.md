# Paimon REST api
Important: currently openapi-generator create ENUMs which use case-sensitive equals method.
In some cases it may produce errors because value will not be found. 
To overcome it use after enum definition in openapi.yaml `x-enum-varnames: ["value1", "value2", ..., "valueN"]`

## Version
- 1.0.0

## Generate
```shell
# iceberg
./gradlew :modules:api:api-iceberg:openApiGenerate
# paimon
./gradlew :modules:api:api-paimon:openApiGenerate
```