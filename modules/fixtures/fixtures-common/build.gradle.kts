plugins {
    id("java-test-fixtures")
}

dependencies {
    testFixturesApi(platform(libs.testcontainers.bom))
    testFixturesApi(platform(libs.junit.bom))

    testFixturesApi(libs.logback)
    testFixturesApi("org.testcontainers:testcontainers")

    testFixturesRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testFixturesRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
}