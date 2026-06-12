import org.gradle.api.tasks.SourceSetContainer
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    // https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/README.adoc
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.jandex)
}

dependencies {
    implementation(project(":modules:platform:platform-jakarta"))
    api(project(":modules:platform:platform-lance"))
}

val rootDir = rootProject.layout.projectDirectory
val specsDir = rootDir.dir("spec")
val lanceSpecDir = specsDir.dir("lance")
val templatesDir = specsDir.dir("server-templates")
val generatedDir = layout.buildDirectory.dir("generated")
val generatedOpenApiSrcDir = layout.buildDirectory.dir("generated/src/main/java")

// https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/jaxrs-resteasy.md
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
// https://github.com/lance-format/lance-namespace/blob/e4708315e087db7da1ef763d84085c66722400ff/docs/src/rest.yaml
openApiGenerate {
    inputSpec.set(lanceSpecDir.file("kasanari-lance-catalog-service.yaml").asFile.absolutePath)
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

    additionalProperties.put("apiNamePrefix", "LanceRest")
    additionalProperties.put("apiNameSuffix", "")
    additionalProperties.put("apiPackage", "kasanari.catalog.lance.api")
    additionalProperties.put("dateLibrary", "java8")

    val lanceModels = listOf(
        "AlterTableAlterColumnsRequest",
        "AlterTableAlterColumnsResponse",
        "AlterTableDropColumnsRequest",
        "AlterTableDropColumnsResponse",
        "AlterTableAddColumnsRequest",
        "AlterTableAddColumnsResponse",
        "AlterTransactionRequest",
        "AlterTransactionResponse",
        "BatchCommitTablesRequest",
        "BatchCommitTablesResponse",
        "BatchCreateTableVersionsRequest",
        "BatchCreateTableVersionsResponse",
        "BatchDeleteTableVersionsRequest",
        "BatchDeleteTableVersionsResponse",
        "CreateNamespaceRequest",
        "CreateNamespaceResponse",
        "CreateTableIndexRequest",
        "CreateTableIndexResponse",
        "CreateTableRequest",
        "CreateTableResponse",
        "CreateTableScalarIndexRequest",
        "CreateTableScalarIndexResponse",
        "CreateTableTagRequest",
        "CreateTableTagResponse",
        "CreateTableVersionRequest",
        "CreateTableVersionResponse",
        "DeclareTableRequest",
        "DeclareTableResponse",
        "DeleteTableTagRequest",
        "DeleteTableTagResponse",
        "DeregisterTableRequest",
        "DeregisterTableResponse",
        "DescribeNamespaceRequest",
        "DescribeNamespaceResponse",
        "DescribeTableIndexStatsRequest",
        "DescribeTableIndexStatsResponse",
        "DescribeTableRequest",
        "DescribeTableResponse",
        "DescribeTableVersionRequest",
        "DescribeTableVersionResponse",
        "DescribeTransactionRequest",
        "DescribeTransactionResponse",
        "DropNamespaceRequest",
        "DropNamespaceResponse",
        "DropTableIndexRequest",
        "DropTableIndexResponse",
        "DropTableRequest",
        "DropTableResponse",
        "ErrorResponse",
        "GetTableStatsRequest",
        "GetTableStatsResponse",
        "GetTableTagVersionRequest",
        "GetTableTagVersionResponse",
        "ListNamespacesResponse",
        "ListTableIndicesRequest",
        "ListTableIndicesResponse",
        "ListTableTagsResponse",
        "ListTableVersionsRequest",
        "ListTableVersionsResponse",
        "ListTablesResponse",
        "NamespaceExistsRequest",
        "RegisterTableRequest",
        "RegisterTableResponse",
        "RenameTableRequest",
        "RenameTableResponse",
        "RestoreTableRequest",
        "RestoreTableResponse",
        "TableExistsRequest",
        "UpdateTableSchemaMetadataRequest",
        "UpdateTableSchemaMetadataResponse",
        "UpdateTableTagRequest",
        "UpdateTableTagResponse",
    )

    val lanceMappings = lanceModels.associateWith { "org.lance.namespace.model.$it" }.toMutableMap()
    modelNameMappings = lanceMappings
    importMappings = lanceMappings.toMutableMap()
}

the<SourceSetContainer>().named("main") {
    java.srcDir(layout.buildDirectory.dir("generated/src/gen/java"))
    java.srcDir(generatedOpenApiSrcDir)
}

tasks.named<GenerateTask>("openApiGenerate") {
    doLast {
        delete(layout.buildDirectory.dir("generated/src/main/java/kasanari/catalog/lance/api/impl"))
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}
