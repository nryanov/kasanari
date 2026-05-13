plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:platform:platform-jackson"))

    api(libs.paimon.core) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    api(libs.paimon.common) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    implementation(project(":modules:repository:repository-jdbc"))
}