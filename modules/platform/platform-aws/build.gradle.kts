plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(enforcedPlatform(libs.aws.platform))
    api("software.amazon.awssdk:aws-core")
    api("software.amazon.awssdk:regions")
    api("software.amazon.awssdk:auth")
    api("software.amazon.awssdk:sdk-core")
    api("software.amazon.awssdk:http-auth")
    api("software.amazon.awssdk:s3-transfer-manager")
    api("software.amazon.awssdk:netty-nio-client")
    api("software.amazon.awssdk:kms")
}