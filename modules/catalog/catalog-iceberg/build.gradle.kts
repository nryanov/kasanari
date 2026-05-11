plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(enforcedPlatform(libs.iceberg.platform)) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    implementation("org.apache.iceberg:iceberg-core")
    implementation("org.apache.iceberg:iceberg-api")
    implementation("org.apache.iceberg:iceberg-data")
    implementation("org.apache.iceberg:iceberg-aws")
    implementation("org.apache.iceberg:iceberg-parquet")
    implementation("org.apache.iceberg:iceberg-orc")
    implementation("org.apache.iceberg:iceberg-nessie")
    implementation("org.apache.iceberg:iceberg-hive-metastore")

    implementation(project(":modules:platform:platform-aws"))
    implementation(project(":modules:platform:platform-hadoop"))
    implementation(project(":modules:repository:repository-jdbc"))

    // test
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-hive")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-nessie")))
}
