plugins {
    id("java-test-fixtures")
}

dependencies {
    api(libs.iceberg.core)

    testFixturesImplementation(platform(libs.junit.bom))
    testFixturesImplementation(libs.junit)
}
