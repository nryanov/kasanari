plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
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
        exclude(group = "org.apache.hive", module = "hive-metastore")
    }

    implementation(project(":modules:platform:platform-aws"))
    implementation(project(":modules:platform:platform-hadoop"))
    implementation(project(":modules:platform:platform-jackson"))
    implementation(project(":modules:repository:repository-jdbc"))
    implementation(project(":modules:repository:repository-paimon:repository-paimon-postgres"))

    // test
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-hive")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
}
