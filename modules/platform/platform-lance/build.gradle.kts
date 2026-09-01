plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:platform:platform-jackson"))
    api(libs.lance.core) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "com.fasterxml.jackson", module = "jackson-bom")
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.datatype")
        exclude(group = "com.fasterxml.jackson.jaxrs")
        exclude(group = "com.fasterxml.jackson.module")
        exclude(group = "com.fasterxml.jackson.dataformat")
    }
    api(libs.lance.namespace.core) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.lance", module = "lance-core")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.fasterxml.jackson", module = "jackson-bom")
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.datatype")
        exclude(group = "com.fasterxml.jackson.jaxrs")
        exclude(group = "com.fasterxml.jackson.module")
        exclude(group = "com.fasterxml.jackson.dataformat")
        exclude(group = "org.apache.commons", module = "commons-lang3")
        exclude(group = "org.apache.opendal", module = "*")
        exclude(group = "org.junit.jupiter", module = "*")
    }
    api("org.lance:lance-namespace-impls-core:0.4.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.lance", module = "lance-core")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.fasterxml.jackson", module = "jackson-bom")
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.datatype")
        exclude(group = "com.fasterxml.jackson.jaxrs")
        exclude(group = "com.fasterxml.jackson.module")
        exclude(group = "com.fasterxml.jackson.dataformat")
    }
}