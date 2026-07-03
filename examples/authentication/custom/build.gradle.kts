plugins {
    `java-library`
}

dependencies {
    implementation(project(":modules:authentication:authentication-spi"))
    implementation(libs.smallrye.mutiny)
}

tasks.jar {
    archiveFileName.set("auth-custom-example.jar")
}
