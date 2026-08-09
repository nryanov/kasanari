plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:core"))
    api(project(":modules:repository:repository-jdbc"))
    api(project(":modules:platform:platform-jackson"))
}