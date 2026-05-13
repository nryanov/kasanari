plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:platform:platform-aws"))
    implementation(project(":modules:platform:platform-hadoop"))
    implementation(project(":modules:platform:platform-lance"))
    implementation(project(":modules:repository:repository-jdbc"))
    implementation(project(":modules:repository:repository-lance:repository-lance-postgres"))
    implementation("org.lance:lance-namespace-hive2:0.3.0")

    // test
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-common")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-hive")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-postgres")))
}
