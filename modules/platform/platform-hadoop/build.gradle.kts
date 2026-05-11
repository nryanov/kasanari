plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(libs.hive.metastore) {
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "asm")
        exclude(group = "org.ow2.asm", module = "asm-all")
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "org.apache.parquet")
        exclude(group = "org.apache.orc")
    }

    api(libs.hadoop3.common) {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "log4j", module = "log4j")
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
        exclude(group = "ch.qos.logback")
    }
    api(libs.hadoop3.client)
    api(libs.hadoop3.aws) {
        exclude(group = "software.amazon.awssdk", module = "bundle")
    }
}