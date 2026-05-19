plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    compileOnly("io.smallrye.reactive:mutiny:2.9.4")
}
