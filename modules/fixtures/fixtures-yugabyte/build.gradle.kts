plugins {
    id("java-test-fixtures")
}

dependencies {
    testFixturesApi(testFixtures(project(":modules:fixtures:fixtures-common")))
    testFixturesApi(project(":modules:repository:repository-jdbc"))
    testFixturesImplementation(libs.jdbc.yugabytedb)
    testFixturesImplementation("org.testcontainers:yugabytedb")
    testFixturesImplementation("org.testcontainers:jdbc")
}
