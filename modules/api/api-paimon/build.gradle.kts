import org.gradle.api.tasks.SourceSetContainer
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    // https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.jandex)
}

dependencies {
    implementation(libs.paimon.api)
}

val rootDir = rootProject.layout.projectDirectory
val specsDir = rootDir.dir("spec")
val paimonSpecDir = specsDir.dir("paimon")
val templatesDir = paimonSpecDir.dir("server-templates")
val generatedDir = layout.buildDirectory.dir("generated")
val generatedOpenApiSrcDir = layout.buildDirectory.dir("generated/src/main/java")

// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/jaxrs-resteasy.md
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
openApiGenerate {
    inputSpec.set(paimonSpecDir.file("paimon-openapi-1.0.0.yaml").asFile.absolutePath)
    generatorName.set("jaxrs-resteasy")
    outputDir.set(generatedDir.get().asFile.absolutePath)
    ignoreFileOverride.set(specsDir.file(".openapi-generator-ignore").asFile.absolutePath)
    removeOperationIdPrefix.set(true)
    // https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/resources/JavaJaxRS/resteasy
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

    additionalProperties.put("apiNamePrefix", "PaimonRest")
    additionalProperties.put("apiNameSuffix", "")
    additionalProperties.put("apiPackage", "kasanari.catalog.paimon.api")
    additionalProperties.put("dateLibrary", "java8")

    modelNameMappings = mutableMapOf()
    importMappings = mutableMapOf()
}

the<SourceSetContainer>().named("main") {
    java.srcDir(layout.buildDirectory.dir("generated/src/gen/java"))
    java.srcDir(generatedOpenApiSrcDir)
}

tasks.named<GenerateTask>("openApiGenerate") {
    doLast {
        delete(layout.buildDirectory.dir("generated/src/main/java/kasanari/catalog/paimon/api/impl"))
    }
}

// TODO: fix generation for paimon
// tasks.named("compileJava") {
//     dependsOn(tasks.named("openApiGenerate"))
// }
