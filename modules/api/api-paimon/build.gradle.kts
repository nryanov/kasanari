import org.gradle.api.tasks.SourceSetContainer
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    // https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.jandex)
}

dependencies {
    api(libs.paimon.api)

    implementation(libs.jakarta.annotation.api)
    implementation(libs.jakarta.inject.api)
    implementation(libs.jakarta.ws.rs.api)
}

val rootDir = rootProject.layout.projectDirectory
val specsDir = rootDir.dir("spec")
val paimonSpecDir = specsDir.dir("paimon")
val templatesDir = paimonSpecDir.dir("server-templates")
val generatedDir = layout.buildDirectory.dir("generated")
val generatedOpenApiSrcDir = layout.buildDirectory.dir("generated/src/main/java")
val paimonOpenApiMappings = mutableMapOf(
    "AlterDatabaseRequest" to "org.apache.paimon.rest.requests.AlterDatabaseRequest",
    "AlterDatabaseResponse" to "org.apache.paimon.rest.responses.AlterDatabaseResponse",
    "AlterFunctionRequest" to "org.apache.paimon.rest.requests.AlterFunctionRequest",
    "AlterTableRequest" to "org.apache.paimon.rest.requests.AlterTableRequest",
    "AlterViewRequest" to "org.apache.paimon.rest.requests.AlterViewRequest",
    "AuthTableQueryRequest" to "org.apache.paimon.rest.requests.AuthTableQueryRequest",
    "AuthTableQueryResponse" to "org.apache.paimon.rest.responses.AuthTableQueryResponse",
    "CommitTableRequest" to "org.apache.paimon.rest.requests.CommitTableRequest",
    "CommitTableResponse" to "org.apache.paimon.rest.responses.CommitTableResponse",
    "ConfigResponse" to "org.apache.paimon.rest.responses.ConfigResponse",
    "CreateBranchRequest" to "org.apache.paimon.rest.requests.CreateBranchRequest",
    "CreateDatabaseRequest" to "org.apache.paimon.rest.requests.CreateDatabaseRequest",
    "CreateFunctionRequest" to "org.apache.paimon.rest.requests.CreateFunctionRequest",
    "CreateTableRequest" to "org.apache.paimon.rest.requests.CreateTableRequest",
    "CreateTagRequest" to "org.apache.paimon.rest.requests.CreateTagRequest",
    "CreateViewRequest" to "org.apache.paimon.rest.requests.CreateViewRequest",
    "ErrorResponse" to "org.apache.paimon.rest.responses.ErrorResponse",
    "ForwardBranchRequest" to "org.apache.paimon.rest.requests.ForwardBranchRequest",
    "GetDatabaseResponse" to "org.apache.paimon.rest.responses.GetDatabaseResponse",
    "GetFunctionResponse" to "org.apache.paimon.rest.responses.GetFunctionResponse",
    "GetTableResponse" to "org.apache.paimon.rest.responses.GetTableResponse",
    "GetTableSnapshotResponse" to "org.apache.paimon.rest.responses.GetTableSnapshotResponse",
    "GetTableDataTokenResponse" to "org.apache.paimon.rest.responses.GetTableTokenResponse",
    "GetTagResponse" to "org.apache.paimon.rest.responses.GetTagResponse",
    "GetVersionSnapshotResponse" to "org.apache.paimon.rest.responses.GetVersionSnapshotResponse",
    "GetViewResponse" to "org.apache.paimon.rest.responses.GetViewResponse",
    "ListBranchesResponse" to "org.apache.paimon.rest.responses.ListBranchesResponse",
    "ListConsumersResponse" to "org.apache.paimon.rest.responses.ListConsumersResponse",
    "ListDatabasesResponse" to "org.apache.paimon.rest.responses.ListDatabasesResponse",
    "ListFunctionDetailsResponse" to "org.apache.paimon.rest.responses.ListFunctionDetailsResponse",
    "ListFunctionsGloballyResponse" to "org.apache.paimon.rest.responses.ListFunctionsGloballyResponse",
    "ListFunctionsResponse" to "org.apache.paimon.rest.responses.ListFunctionsResponse",
    "ListPartitionsByNamesRequest" to "org.apache.paimon.rest.requests.ListPartitionsByNamesRequest",
    "ListPartitionsResponse" to "org.apache.paimon.rest.responses.ListPartitionsResponse",
    "ListSnapshotsResponse" to "org.apache.paimon.rest.responses.ListSnapshotsResponse",
    "ListTableDetailsResponse" to "org.apache.paimon.rest.responses.ListTableDetailsResponse",
    "ListTablesGloballyResponse" to "org.apache.paimon.rest.responses.ListTablesGloballyResponse",
    "ListTablesResponse" to "org.apache.paimon.rest.responses.ListTablesResponse",
    "ListTagsResponse" to "org.apache.paimon.rest.responses.ListTagsResponse",
    "ListViewDetailsResponse" to "org.apache.paimon.rest.responses.ListViewDetailsResponse",
    "ListViewsGloballyResponse" to "org.apache.paimon.rest.responses.ListViewsGloballyResponse",
    "ListViewsResponse" to "org.apache.paimon.rest.responses.ListViewsResponse",
    "MarkDonePartitionsRequest" to "org.apache.paimon.rest.requests.MarkDonePartitionsRequest",
    "RegisterTableRequest" to "org.apache.paimon.rest.requests.RegisterTableRequest",
    "RenameBranchRequest" to "org.apache.paimon.rest.requests.RenameBranchRequest",
    "RenameTableRequest" to "org.apache.paimon.rest.requests.RenameTableRequest",
    "ResetConsumerRequest" to "org.apache.paimon.rest.requests.ResetConsumerRequest",
    "RollbackSchemaRequest" to "org.apache.paimon.rest.requests.RollbackSchemaRequest",
    "RollbackTableRequest" to "org.apache.paimon.rest.requests.RollbackTableRequest",
)

// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/jaxrs-resteasy.md
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
// https://github.com/apache/paimon/blob/release-1.4.1/docs/static/rest-catalog-open-api.yaml
openApiGenerate {
    inputSpec.set(paimonSpecDir.file("paimon-openapi-1.4.1.yaml").asFile.absolutePath)
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

    modelNameMappings = paimonOpenApiMappings.toMutableMap()
    importMappings = paimonOpenApiMappings.toMutableMap()
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

 tasks.named("compileJava") {
     dependsOn(tasks.named("openApiGenerate"))
 }
