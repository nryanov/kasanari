plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:platform:platform-aws"))
    implementation(project(":modules:platform:platform-hadoop"))
    implementation(project(":modules:platform:platform-lance"))
    implementation(project(":modules:platform:platform-jackson"))

    implementation(project(":modules:repository:repository-jdbc"))
    implementation(project(":modules:repository:repository-lance:repository-lance-postgres"))

    implementation("org.lance:lance-namespace-hive2:0.3.0") {
        exclude(group = "org.lance", module = "lance-core")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.fasterxml.jackson.core", module = "*")
        exclude(group = "com.fasterxml.jackson.datatype", module = "*")
        exclude(group = "com.fasterxml.jackson.jaxrs", module = "jackson-jaxrs-json-provider")
        exclude(group = "org.apache.commons", module = "commons-lang3")
        exclude(group = "org.apache.opendal", module = "*")
        exclude(group = "org.junit.jupiter", module = "*")
        exclude(group = "org.apache.hive", module = "hive-metastore")
    }

    // test
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-hive")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
}
