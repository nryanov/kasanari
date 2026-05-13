plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:platform:platform-lance"))
    api("org.lance:lance-namespace-impls-core:0.3.0")
}