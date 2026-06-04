plugins {
    `java-library`
}

dependencies {
    implementation(project(":modules:authentication:authentication-spi"))
    implementation("io.smallrye.reactive:mutiny:2.9.4")
}

tasks.jar {
    archiveBaseName.set("auth-custom-example")
}
