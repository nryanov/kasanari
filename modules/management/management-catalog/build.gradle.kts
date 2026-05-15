plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:repository:repository-jdbc"))
    api(project(":modules:repository:repository-management:repository-management-catalog:repository-management-catalog-postgres"))
    implementation(project(":modules:platform:platform-jackson"))
}