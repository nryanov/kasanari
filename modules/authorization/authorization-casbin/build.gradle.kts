plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:authorization:authorization-spi"))
    implementation(libs.casbin.jcasbin)
    implementation(project(":modules:repository:repository-jdbc"))
    api(project(":modules:repository:repository-management:repository-management-security:repository-management-security-postgres"))

    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
}
