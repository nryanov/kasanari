dependencies {
    implementation(project(":modules:catalog:catalog-iceberg-core"))
    implementation(project(":modules:catalog:catalog-iceberg-common"))

    implementation(libs.iceberg.api)
    implementation(libs.iceberg.common)

    implementation(libs.jdbi.core)
    implementation(libs.agroal.pool)

    testImplementation(testFixtures(project(":modules:catalog:catalog-iceberg-core")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
}
