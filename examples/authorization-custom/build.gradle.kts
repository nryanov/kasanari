plugins {
    `java-library`
}

dependencies {
    implementation(project(":modules:authorization:authorization-spi"))
}

tasks.jar {
    archiveBaseName.set("authorization-custom-example")
}
