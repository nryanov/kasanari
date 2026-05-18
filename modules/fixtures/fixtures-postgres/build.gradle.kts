plugins {
    id("java-test-fixtures")
}

dependencies {
    testFixturesApi(testFixtures(project(":modules:fixtures:fixtures-common")))
    testFixturesImplementation(libs.jdbc.postgresql)
    testFixturesImplementation("org.testcontainers:postgresql")
}
