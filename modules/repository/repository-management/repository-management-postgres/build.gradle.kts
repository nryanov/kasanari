plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:platform:platform-jackson"))
    implementation(project(":modules:repository:repository-jdbc"))
    api(project(":modules:repository:repository-management"))
}