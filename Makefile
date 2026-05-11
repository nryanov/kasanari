clean:
	./gradlew clean

compile:
	./gradlew compileJava

deps:
	./gradlew :modules:catalog:catalog-iceberg-kasanari:dependencies

iceberg:
	./gradlew :modules:api:api-iceberg:openApiGenerate

paimon:
	./gradlew :modules:api:api-paimon:openApiGenerate

lance:
	./gradlew :modules:api:api-lance:openApiGenerate

management:
	./gradlew :modules:api:api-management:openApiGenerate