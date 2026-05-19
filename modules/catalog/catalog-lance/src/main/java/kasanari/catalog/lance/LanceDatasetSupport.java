package kasanari.catalog.lance;

import org.apache.arrow.memory.BufferAllocator;
import org.lance.Dataset;
import org.lance.index.IndexOptions;
import org.lance.index.IndexParams;
import org.lance.index.IndexType;
import org.lance.namespace.model.AlterColumnsEntry;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableAlterColumnsResponse;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsResponse;
import org.lance.namespace.model.CreateTableIndexRequest;
import org.lance.namespace.model.CreateTableIndexResponse;
import org.lance.namespace.model.CreateTableScalarIndexResponse;
import org.lance.namespace.model.CreateTableTagRequest;
import org.lance.namespace.model.CreateTableTagResponse;
import org.lance.namespace.model.DeleteTableTagRequest;
import org.lance.namespace.model.DeleteTableTagResponse;
import org.lance.namespace.model.DescribeTableIndexStatsRequest;
import org.lance.namespace.model.DescribeTableIndexStatsResponse;
import org.lance.namespace.model.DropTableIndexRequest;
import org.lance.namespace.model.DropTableIndexResponse;
import org.lance.namespace.model.GetTableStatsRequest;
import org.lance.namespace.model.GetTableStatsResponse;
import org.lance.namespace.model.GetTableTagVersionRequest;
import org.lance.namespace.model.GetTableTagVersionResponse;
import org.lance.namespace.model.IndexContent;
import org.lance.namespace.model.ListTableIndicesRequest;
import org.lance.namespace.model.ListTableIndicesResponse;
import org.lance.namespace.model.ListTableTagsRequest;
import org.lance.namespace.model.ListTableTagsResponse;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.RestoreTableResponse;
import org.lance.namespace.model.TagContents;
import org.lance.namespace.model.UpdateTableSchemaMetadataRequest;
import org.lance.namespace.model.UpdateTableSchemaMetadataResponse;
import org.lance.namespace.model.UpdateTableTagRequest;
import org.lance.namespace.model.UpdateTableTagResponse;
import org.lance.schema.ColumnAlteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class LanceDatasetSupport {
    private LanceDatasetSupport() {
    }

    static RestoreTableResponse restoreTable(BufferAllocator allocator, String location, RestoreTableRequest request) {
        try (var dataset = open(allocator, location)) {
            dataset.checkoutVersion(request.getVersion());
            dataset.restore();
            return new RestoreTableResponse();
        }
    }

    static AlterTableAlterColumnsResponse alterTableAlterColumns(
            BufferAllocator allocator,
            String location,
            AlterTableAlterColumnsRequest request
    ) {
        try (var dataset = open(allocator, location)) {
            var alterations = new ArrayList<ColumnAlteration>();
            for (AlterColumnsEntry entry : request.getAlterations()) {
                var builder = new ColumnAlteration.Builder(entry.getPath());
                if (entry.getRename() != null) {
                    builder.rename(entry.getRename());
                }
                if (entry.getNullable() != null) {
                    builder.nullable(entry.getNullable());
                }
                alterations.add(builder.build());
            }
            dataset.alterColumns(alterations);
            return new AlterTableAlterColumnsResponse().version(dataset.version());
        }
    }

    static AlterTableDropColumnsResponse alterTableDropColumns(
            BufferAllocator allocator,
            String location,
            AlterTableDropColumnsRequest request
    ) {
        try (var dataset = open(allocator, location)) {
            dataset.dropColumns(request.getColumns());
            return new AlterTableDropColumnsResponse().version(dataset.version());
        }
    }

    private static Dataset open(BufferAllocator allocator, String location) {
        return Dataset.open(location, allocator);
    }
}
