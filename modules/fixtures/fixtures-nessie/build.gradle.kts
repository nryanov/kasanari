plugins {
    id("java-test-fixtures")
}

dependencies {
    testFixturesApi(testFixtures(project(":modules:fixtures:fixtures-common")))
    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation(libs.testcontainers.nessie)
}
