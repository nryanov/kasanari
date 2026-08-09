plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:repository:repository-jdbc"))
    api(project(":modules:repository:repository-management:repository-management-security"))

    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-yugabyte")))
}