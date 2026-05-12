package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestTableService;
import kasanari.server.infrastructure.http.ApiFallbacks;
import org.lance.namespace.model.AlterTableAddColumnsRequest;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.AnalyzeTableQueryPlanRequest;
import org.lance.namespace.model.CountTableRowsRequest;
import org.lance.namespace.model.CreateTableIndexRequest;
import org.lance.namespace.model.CreateTableTagRequest;
import org.lance.namespace.model.DeleteFromTableRequest;
import org.lance.namespace.model.DeleteTableTagRequest;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DescribeTableIndexStatsRequest;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DropTableIndexRequest;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.ExplainTableQueryPlanRequest;
import org.lance.namespace.model.GetTableStatsRequest;
import org.lance.namespace.model.GetTableTagVersionRequest;
import org.lance.namespace.model.ListTableIndicesRequest;
import org.lance.namespace.model.ListTableVersionsRequest;
import org.lance.namespace.model.QueryTableRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.TableExistsRequest;
import org.lance.namespace.model.UpdateTableRequest;
import org.lance.namespace.model.UpdateTableTagRequest;

import java.io.File;

@ApplicationScoped
public class LanceTableServiceHandler implements LanceRestTableService {
    @Override
    public Response alterTableAddColumns(String id, AlterTableAddColumnsRequest orgLanceNamespaceModelAlterTableAddColumnsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.alterTableAddColumns");
    }

    @Override
    public Response alterTableAlterColumns(String id, AlterTableAlterColumnsRequest orgLanceNamespaceModelAlterTableAlterColumnsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.alterTableAlterColumns");
    }

    @Override
    public Response alterTableDropColumns(String id, AlterTableDropColumnsRequest orgLanceNamespaceModelAlterTableDropColumnsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.alterTableDropColumns");
    }

    @Override
    public Response analyzeTableQueryPlan(String id, AnalyzeTableQueryPlanRequest orgLanceNamespaceModelAnalyzeTableQueryPlanRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.analyzeTableQueryPlan");
    }

    @Override
    public Response countTableRows(String id, CountTableRowsRequest orgLanceNamespaceModelCountTableRowsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.countTableRows");
    }

    @Override
    public Response createTable(String id, File body, String delimiter, String xLanceTableLocation, String xLanceTableProperties, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.createTable");
    }

    @Override
    public Response createTableIndex(String id, CreateTableIndexRequest orgLanceNamespaceModelCreateTableIndexRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.createTableIndex");
    }

    @Override
    public Response createTableTag(String id, CreateTableTagRequest orgLanceNamespaceModelCreateTableTagRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.createTableTag");
    }

    @Override
    public Response deleteFromTable(String id, DeleteFromTableRequest orgLanceNamespaceModelDeleteFromTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.deleteFromTable");
    }

    @Override
    public Response deleteTableTag(String id, DeleteTableTagRequest orgLanceNamespaceModelDeleteTableTagRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.deleteTableTag");
    }

    @Override
    public Response deregisterTable(String id, DeregisterTableRequest orgLanceNamespaceModelDeregisterTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.deregisterTable");
    }

    @Override
    public Response describeTable(String id, DescribeTableRequest orgLanceNamespaceModelDescribeTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.describeTable");
    }

    @Override
    public Response describeTableIndexStats(String id, String indexName, DescribeTableIndexStatsRequest orgLanceNamespaceModelDescribeTableIndexStatsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.describeTableIndexStats");
    }

    @Override
    public Response dropTable(String id, DropTableRequest orgLanceNamespaceModelDropTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.dropTable");
    }

    @Override
    public Response dropTableIndex(String id, String indexName, DropTableIndexRequest orgLanceNamespaceModelDropTableIndexRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.dropTableIndex");
    }

    @Override
    public Response explainTableQueryPlan(String id, ExplainTableQueryPlanRequest orgLanceNamespaceModelExplainTableQueryPlanRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.explainTableQueryPlan");
    }

    @Override
    public Response getTableStats(String id, GetTableStatsRequest orgLanceNamespaceModelGetTableStatsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.getTableStats");
    }

    @Override
    public Response getTableTagVersion(String id, GetTableTagVersionRequest orgLanceNamespaceModelGetTableTagVersionRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.getTableTagVersion");
    }

    @Override
    public Response insertIntoTable(String id, File body, String delimiter, String mode, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.insertIntoTable");
    }

    @Override
    public Response listTableIndices(String id, ListTableIndicesRequest orgLanceNamespaceModelListTableIndicesRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.listTableIndices");
    }

    @Override
    public Response listTableTags(String id, String delimiter, String pageToken, Integer limit, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.listTableTags");
    }

    @Override
    public Response listTableVersions(String id, ListTableVersionsRequest orgLanceNamespaceModelListTableVersionsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.listTableVersions");
    }

    @Override
    public Response mergeInsertIntoTable(String id, String on, File body, String delimiter, Boolean whenMatchedDelete, Boolean whenMatchedUpdateAll, String whenMatchedUpdateAllFilt, Boolean whenNotMatchedInsertAll, Boolean whenNotMatchedBySourceDelete, String whenNotMatchedBySourceDeleteFilt, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.mergeInsertIntoTable");
    }

    @Override
    public Response queryTable(String id, QueryTableRequest orgLanceNamespaceModelQueryTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.queryTable");
    }

    @Override
    public Response registerTable(String id, RegisterTableRequest orgLanceNamespaceModelRegisterTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.registerTable");
    }

    @Override
    public Response restoreTable(String id, RestoreTableRequest orgLanceNamespaceModelRestoreTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.restoreTable");
    }

    @Override
    public Response tableExists(String id, TableExistsRequest orgLanceNamespaceModelTableExistsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.tableExists");
    }

    @Override
    public Response updateTable(String id, UpdateTableRequest orgLanceNamespaceModelUpdateTableRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.updateTable");
    }

    @Override
    public Response updateTableTag(String id, UpdateTableTagRequest orgLanceNamespaceModelUpdateTableTagRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTableService.updateTableTag");
    }
}
