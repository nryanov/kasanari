plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:platform:platform-iceberg"))
    implementation(project(":modules:platform:platform-aws"))
    implementation(project(":modules:platform:platform-hadoop"))
    implementation(project(":modules:platform:platform-jackson"))
    implementation(project(":modules:repository:repository-jdbc"))
    implementation(project(":modules:repository:repository-iceberg:repository-iceberg-postgres"))
    implementation(project(":modules:repository:repository-iceberg:repository-iceberg-yugabyte"))

    implementation("org.apache.iceberg:iceberg-core")
    implementation("org.apache.iceberg:iceberg-api")
    implementation("org.apache.iceberg:iceberg-data")
    implementation("org.apache.iceberg:iceberg-aws")
    implementation("org.apache.iceberg:iceberg-parquet")
    implementation("org.apache.iceberg:iceberg-orc")
    implementation("org.apache.iceberg:iceberg-nessie")
    implementation("org.apache.iceberg:iceberg-hive-metastore")

    // test
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-hive")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-nessie")))
}
