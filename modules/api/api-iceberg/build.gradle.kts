import org.gradle.api.tasks.SourceSetContainer
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    // https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.jandex)
}

dependencies {
    implementation(libs.iceberg.api)
    implementation(libs.iceberg.core)
    implementation(libs.iceberg.common)

    implementation(libs.jakarta.annotation.api)
    implementation(libs.jakarta.inject.api)
    implementation(libs.jakarta.ws.rs.api)
}

val rootDir = rootProject.layout.projectDirectory
val specsDir = rootDir.dir("spec")
val icebergSpecDir = specsDir.dir("iceberg")
val templatesDir = icebergSpecDir.dir("server-templates")
val generatedDir = layout.buildDirectory.dir("generated")
val generatedOpenApiSrcDir = layout.buildDirectory.dir("generated/src/main/java")

// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/jaxrs-resteasy.md
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
openApiGenerate {
    inputSpec.set(icebergSpecDir.file("kasanari-catalog-service.yaml").asFile.absolutePath)
    generatorName.set("jaxrs-resteasy")
    outputDir.set(generatedDir.get().asFile.absolutePath)
    ignoreFileOverride.set(specsDir.file(".openapi-generator-ignore").asFile.absolutePath)
    removeOperationIdPrefix.set(true)
    // https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/resources/JavaJaxRS/resteasy
    templateDir.set(templatesDir.asFile.absolutePath)

    globalProperties.put("apiDocs", "false")
    globalProperties.put("models", "false")
    globalProperties.put("apis", "CatalogApi,ConfigurationApi")
    globalProperties.put("modelTests", "false")

    serverVariables.put("basePath", "api/catalog")

    configOptions.put("resourceName", "catalog")
    configOptions.put("useTags", "true")
    configOptions.put("useBeanValidation", "false")
    configOptions.put("sourceFolder", "src/main/java")
    configOptions.put("useJakartaEe", "true")
    configOptions.put("hideGenerationTimestamp", "true")

    additionalProperties.put("apiNamePrefix", "IcebergRest")
    additionalProperties.put("apiNameSuffix", "")
    additionalProperties.put("apiPackage", "kasanari.catalog.iceberg.api")
    additionalProperties.put("dateLibrary", "java8")

    modelNameMappings = mutableMapOf(
        "CatalogConfig" to "org.apache.iceberg.rest.responses.ConfigResponse",
        "LoadTableResult" to "org.apache.iceberg.rest.responses.LoadTableResponse",
        "LoadViewResult" to "org.apache.iceberg.rest.responses.LoadViewResponse",
        "CommitTableResponse" to "org.apache.iceberg.rest.responses.LoadTableResponse",
        "CommitViewRequest" to "org.apache.iceberg.rest.requests.UpdateTableRequest",
        "CommitTableRequest" to "org.apache.iceberg.rest.requests.UpdateTableRequest",
        "IcebergErrorResponse" to "org.apache.iceberg.rest.responses.ErrorResponse",
    )

    importMappings = mutableMapOf(
        "CatalogConfig" to "org.apache.iceberg.rest.responses.ConfigResponse",
        "CommitTableResponse" to "org.apache.iceberg.rest.responses.LoadTableResponse",
        "CreateNamespaceRequest" to "org.apache.iceberg.rest.requests.CreateNamespaceRequest",
        "CreateNamespaceResponse" to "org.apache.iceberg.rest.responses.CreateNamespaceResponse",
        "CreateTableRequest" to "org.apache.iceberg.rest.requests.CreateTableRequest",
        "ErrorModel" to "org.apache.iceberg.rest.responses.ErrorResponse",
        "GetNamespaceResponse" to "org.apache.iceberg.rest.responses.GetNamespaceResponse",
        "ListNamespacesResponse" to "org.apache.iceberg.rest.responses.ListNamespacesResponse",
        "ListTablesResponse" to "org.apache.iceberg.rest.responses.ListTablesResponse",
        "LoadCredentialsResponse" to "org.apache.iceberg.rest.responses.LoadCredentialsResponse",
        "LoadTableResult" to "org.apache.iceberg.rest.responses.LoadTableResponse",
        "LoadViewResult" to "org.apache.iceberg.rest.responses.LoadTableResponse",
        "OAuthTokenResponse" to "org.apache.iceberg.rest.responses.OAuthTokenResponse",
        "OAuthErrorResponse" to "org.apache.iceberg.rest.responses.OAuthErrorResponse",
        "RenameTableRequest" to "org.apache.iceberg.rest.requests.RenameTableRequest",
        "ReportMetricsRequest" to "org.apache.iceberg.rest.requests.ReportMetricsRequest",
        "UpdateNamespacePropertiesRequest" to "org.apache.iceberg.rest.requests.UpdateNamespacePropertiesRequest",
        "UpdateNamespacePropertiesResponse" to "org.apache.iceberg.rest.responses.UpdateNamespacePropertiesResponse",
        "CommitTransactionRequest" to "org.apache.iceberg.rest.requests.CommitTransactionRequest",
        "CreateViewRequest" to "org.apache.iceberg.rest.requests.CreateViewRequest",
        "RegisterTableRequest" to "org.apache.iceberg.rest.requests.RegisterTableRequest",
        "IcebergErrorResponse" to "org.apache.iceberg.rest.responses.ErrorResponse",
        "OAuthError" to "org.apache.iceberg.rest.responses.ErrorResponse",
        "CommitViewRequest" to "org.apache.iceberg.rest.requests.UpdateTableRequest",
        "CommitTableRequest" to "org.apache.iceberg.rest.requests.UpdateTableRequest",
    )
}

the<SourceSetContainer>().named("main") {
    java.srcDir(generatedOpenApiSrcDir)
}

tasks.named<GenerateTask>("openApiGenerate") {
    doLast {
        delete(layout.buildDirectory.dir("generated/src/main/java/kasanari/catalog/iceberg/api/impl"))
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}
