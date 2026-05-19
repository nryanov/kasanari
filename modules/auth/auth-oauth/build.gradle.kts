plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))

    implementation(project(":modules:auth:auth-spi"))
    implementation("io.smallrye.reactive:mutiny:2.9.4")
    implementation("io.quarkus:quarkus-oidc-common")
    implementation("org.bitbucket.b_c:jose4j")
}
