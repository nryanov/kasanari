plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:platform:platform-lance"))
    api(project(":modules:repository:repository-jdbc"))
}