dependencies {
    implementation(project(":modules:catalog:catalog-iceberg-core"))
    implementation(project(":modules:catalog:catalog-iceberg-common"))

    testImplementation(testFixtures(project(":modules:catalog:catalog-iceberg-core")))
    testImplementation(testFixtures(project(":modules:fixtures:fixtures-s3")))

    implementation(libs.hadoop3.common) {
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
        exclude(group = "ch.qos.reload4j", module = "reload4j")
        exclude(group = "log4j", module = "log4j")
        exclude(group = "org.apache.zookeeper", module = "zookeeper")
        exclude(group = "org.apache.hadoop.thirdparty", module = "hadoop-shaded-protobuf_3_25")
        exclude(group = "com.github.pjfanning", module = "jersey-json")
        exclude(group = "com.sun.jersey", module = "jersey-core")
        exclude(group = "com.sun.jersey", module = "jersey-server")
        exclude(group = "com.sun.jersey", module = "jersey-servlet")
        exclude(group = "com.sun.jersey", module = "jersey-servlet")
        exclude(group = "io.dropwizard.metrics", module = "metrics-core")
    }
    implementation(libs.hadoop3.aws) {
        exclude(group = "software.amazon.awssdk", module = "bundle")
    }
}
