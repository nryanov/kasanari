clean:
	./gradlew clean

compile:
	./gradlew compileJava

deps:
	./gradlew :modules:catalog:catalog-iceberg-kasanari:dependencies