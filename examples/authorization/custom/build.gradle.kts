plugins {
    `java-library`
}

dependencies {
    implementation(project(":modules:authorization:authorization-spi"))
}

tasks.jar {
    archiveFileName.set("authorization-custom-example.jar")
}
