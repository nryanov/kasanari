plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:repository:repository-jdbc"))
    implementation(project(":modules:platform:platform-jackson"))
    api(project(":modules:repository:repository-paimon"))

    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
}