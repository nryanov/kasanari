plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(libs.smallrye.mutiny)
    implementation(enforcedPlatform(libs.quarkus.bom))

    implementation(project(":modules:auth:auth-spi"))
    implementation("io.quarkus:quarkus-oidc-common")
    implementation("org.bitbucket.b_c:jose4j")
}
