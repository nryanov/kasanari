plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:platform:platform-iceberg"))
    implementation(project(":modules:repository:repository-jdbc"))

    api("org.apache.iceberg:iceberg-core")
}