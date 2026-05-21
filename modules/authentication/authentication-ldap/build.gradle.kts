plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))

    implementation(libs.smallrye.mutiny)
    implementation(project(":modules:authentication:authentication-spi"))
    implementation("org.wildfly.security:wildfly-elytron-realm-ldap")
    implementation("org.wildfly.security:wildfly-elytron-credential")
}
