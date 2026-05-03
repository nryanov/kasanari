plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(enforcedPlatform(libs.aws.platform))
    implementation("software.amazon.awssdk:aws-core")
    implementation("software.amazon.awssdk:regions")
    implementation("software.amazon.awssdk:auth")
    implementation("software.amazon.awssdk:sdk-core")
    implementation("software.amazon.awssdk:http-auth")
    implementation("software.amazon.awssdk:s3-transfer-manager")
    implementation("software.amazon.awssdk:netty-nio-client")

    implementation(libs.paimon.core) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.paimon.common) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.paimon.s3) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.paimon.format) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.paimon.hive.catalog) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "asm")
        exclude(group = "org.ow2.asm", module = "asm-all")
        exclude(group = "org.apache.logging.log4j")
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
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-hive")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
}
