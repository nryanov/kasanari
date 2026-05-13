plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(libs.lance.core)
    api(libs.lance.namespace.core) {
        exclude(group = "org.lance", module = "lance-core")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.fasterxml.jackson.core", module = "*")
        exclude(group = "com.fasterxml.jackson.datatype", module = "*")
        exclude(group = "com.fasterxml.jackson.jaxrs", module = "jackson-jaxrs-json-provider")
        exclude(group = "org.apache.commons", module = "commons-lang3")
        exclude(group = "org.apache.opendal", module = "*")
        exclude(group = "org.junit.jupiter", module = "*")
    }
    api("org.lance:lance-namespace-impls-core:0.3.0")
}