import org.gradle.api.tasks.SourceSetContainer
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.jandex)
}

dependencies {
    implementation(project(":modules:platform:platform-jakarta"))
}

val rootDir = rootProject.layout.projectDirectory
val specsDir = rootDir.dir("spec")
val managementSpecDir = specsDir.dir("management")
val templatesDir = specsDir.dir("server-templates")
val generatedDir = layout.buildDirectory.dir("generated")
val generatedOpenApiSrcDir = layout.buildDirectory.dir("generated/src/main/java")

openApiGenerate {
    inputSpec.set(managementSpecDir.file("kasanari-management-catalog-service.yaml").asFile.absolutePath)
    generatorName.set("jaxrs-resteasy")
    outputDir.set(generatedDir.get().asFile.absolutePath)
    ignoreFileOverride.set(specsDir.file(".openapi-generator-ignore").asFile.absolutePath)
    removeOperationIdPrefix.set(true)
    templateDir.set(templatesDir.asFile.absolutePath)

    globalProperties.put("apiDocs", "false")
    globalProperties.put("models", "false")
    globalProperties.put("apis", "")
    globalProperties.put("modelTests", "false")

    serverVariables.put("basePath", "api/catalog")

    configOptions.put("resourceName", "catalog")
    configOptions.put("useTags", "true")
    configOptions.put("useBeanValidation", "false")
    configOptions.put("sourceFolder", "src/main/java")
    configOptions.put("useJakartaEe", "true")
    configOptions.put("hideGenerationTimestamp", "true")

    additionalProperties.put("apiNamePrefix", "ManagementRest")
    additionalProperties.put("apiNameSuffix", "")
    additionalProperties.put("modelNameSuffix", "Dto")
    additionalProperties.put("apiPackage", "kasanari.catalog.management.api")
    additionalProperties.put("modelPackage", "kasanari.catalog.management.dto")
    additionalProperties.put("dateLibrary", "java8")
}

tasks.named<GenerateTask>("openApiGenerate") {
    doLast {
        delete(layout.buildDirectory.dir("generated/src/main/java/kasanari/catalog/management/api/impl"))
    }
}


the<SourceSetContainer>().named("main") {
    java.srcDir(generatedOpenApiSrcDir)
}

tasks.named("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}
