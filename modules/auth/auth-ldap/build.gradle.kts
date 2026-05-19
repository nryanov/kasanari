plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))

    implementation(project(":modules:auth:auth-spi"))
    implementation("io.smallrye.reactive:mutiny:2.9.4")
    implementation("org.wildfly.security:wildfly-elytron-realm-ldap")
    implementation("org.wildfly.security:wildfly-elytron-credential")
}
