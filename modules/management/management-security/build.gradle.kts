plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:api:api-management"))
    implementation(libs.casbin.jcasbin)
    implementation(project(":modules:repository:repository-jdbc"))
    api(project(":modules:repository:repository-management:repository-management-security:repository-management-security-postgres"))
    implementation(project(":modules:platform:platform-jakarta"))
}