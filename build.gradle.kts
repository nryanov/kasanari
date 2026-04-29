import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test

plugins {
    id("java")
}

allprojects {
    group = "kasanari"
    version = "0.1.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        add("implementation", "org.slf4j:slf4j-api:2.0.17")

        add("testImplementation", "ch.qos.logback:logback-classic:1.5.18")
        add("testImplementation", platform("org.junit:junit-bom:5.12.2"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter:5.12.2")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher:1.12.2")
    }

    tasks.named<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.named<JavaCompile>("compileTestJava") {
        options.encoding = "UTF-8"
    }
}
