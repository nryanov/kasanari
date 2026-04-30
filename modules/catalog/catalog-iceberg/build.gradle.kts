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

    implementation(enforcedPlatform(libs.aws.platform))
    implementation("software.amazon.awssdk:aws-core")
    implementation("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
    implementation("software.amazon.awssdk:sdk-core")
    implementation("software.amazon.awssdk:http-auth")
    implementation("software.amazon.awssdk:s3-transfer-manager")
    implementation("software.amazon.awssdk:netty-nio-client")

    implementation(libs.hive.metastore) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "asm")
        exclude(group = "org.ow2.asm", module = "asm-all")
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "org.apache.parquet")
        exclude(group = "org.apache.orc")
    }

    implementation(libs.hadoop3.common) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "log4j", module = "log4j")
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
        exclude(group = "ch.qos.logback")
    }
    implementation(libs.hadoop3.client)
    implementation(libs.hadoop3.aws) {
        exclude(group = "software.amazon.awssdk", module = "bundle")
    }

    implementation(libs.jdbi.core)
    implementation(libs.agroal.pool)

    // test
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(platform(libs.junit.bom))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.junit.jupiter:junit-jupiter-api")

    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-hive")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-nessie")))
}
