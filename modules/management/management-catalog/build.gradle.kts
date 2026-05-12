plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:api:api-management"))
    implementation(project(":modules:repository:repository-jdbc"))
    implementation(project(":modules:repository:repository-management:repository-management-postgres"))
}