val kasanariVersion: String by project
val mutinyVersion: String by project

dependencies {
    implementation("com.nryanov.kasanari:authentication-spi:$kasanariVersion")
    implementation("io.smallrye.reactive:mutiny:$mutinyVersion")
}

tasks.jar {
    archiveFileName.set("auth-custom-example.jar")
}
