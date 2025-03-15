clean:
	./gradlew clean

compile:
	./gradlew compileJava

docker-build:
	./gradlew :modules:core:build
