package kasanari.server.iceberg.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.iceberg.dto.IcebergCommitTableRequestDto;
import kasanari.api.iceberg.dto.IcebergTableRequirementDto;
import kasanari.api.iceberg.dto.IcebergTableUpdateDto;
import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergTable;
import kasanari.catalog.iceberg.core.model.IcebergValues;
import kasanari.catalog.iceberg.core.model.IcebergView;

@ApplicationScoped
public class RestMapper {
    public IcebergNamespace.Name namespaceName(String namespaceName) {
        return new IcebergNamespace.Name(namespaceName);
    }

    public IcebergView.Name viewName(String viewName) {
        return new IcebergView.Name(viewName);
    }

    public IcebergTable.Name tableName(String tableName) {
        return new IcebergTable.Name(tableName);
    }

    public IcebergTable table(String namespaceName, String tableName) {
        return new IcebergTable(namespaceName(namespaceName), tableName(tableName));
    }

    public IcebergTable.UpdateRequest tableUpdateRequest(IcebergCommitTableRequestDto rq) {
        return new IcebergTable.UpdateRequest(
                rq.getRequirements().stream().map(this::tableRequirement).toList(),
                rq.getUpdates().stream().map(this::tableUpdate).toList()
        );
    }

    public IcebergTable.UpdateRequest.Update tableUpdate(IcebergTableUpdateDto dto) {
        return switch (dto.getAction()) {
            case "assign-uuid" -> new IcebergTable.UpdateRequest.Update.AssignUuidUpdate(new IcebergValues.Uuid(""));
            default -> throw new IllegalArgumentException(String.format("Unknown table update type: %s", dto.getAction()));
/*
          assign-uuid: '#/components/schemas/AssignUUIDUpdate'
          upgrade-format-version: '#/components/schemas/UpgradeFormatVersionUpdate'
          add-schema: '#/components/schemas/AddSchemaUpdate'
          set-current-schema: '#/components/schemas/SetCurrentSchemaUpdate'
          add-spec: '#/components/schemas/AddPartitionSpecUpdate'
          set-default-spec: '#/components/schemas/SetDefaultSpecUpdate'
          add-sort-order: '#/components/schemas/AddSortOrderUpdate'
          set-default-sort-order: '#/components/schemas/SetDefaultSortOrderUpdate'
          add-snapshot: '#/components/schemas/AddSnapshotUpdate'
          set-snapshot-ref: '#/components/schemas/SetSnapshotRefUpdate'
          remove-snapshots: '#/components/schemas/RemoveSnapshotsUpdate'
          remove-snapshot-ref: '#/components/schemas/RemoveSnapshotRefUpdate'
          set-location: '#/components/schemas/SetLocationUpdate'
          set-properties: '#/components/schemas/SetPropertiesUpdate'
          remove-properties: '#/components/schemas/RemovePropertiesUpdate'
          add-view-version: '#/components/schemas/AddViewVersionUpdate'
          set-current-view-version: '#/components/schemas/SetCurrentViewVersionUpdate'
          set-statistics: '#/components/schemas/SetStatisticsUpdate'
          remove-statistics: '#/components/schemas/RemoveStatisticsUpdate'
          set-partition-statistics: '#/components/schemas/SetPartitionStatisticsUpdate'
          remove-partition-statistics: '#/components/schemas/RemovePartitionStatisticsUpdate'
          remove-partition-specs: '#/components/schemas/RemovePartitionSpecsUpdate'
          enable-row-lineage: '#/components/schemas/EnableRowLineageUpdate'
 */
        };
    }

    public IcebergTable.UpdateRequest.Requirement tableRequirement(IcebergTableRequirementDto dto) {
        return switch (dto.getType()) {
            case "assert-create" -> new IcebergTable.UpdateRequest.Requirement.AssertCreate();
            case "assert-table-uuid" -> new IcebergTable.UpdateRequest.Requirement.AssertTableUuid(new IcebergValues.Uuid(""));
            default -> throw new IllegalArgumentException(String.format("Unknown table requirement type: %s", dto.getType()));

            /*
assert-table-uuid: '#/components/schemas/AssertTableUUID'
assert-ref-snapshot-id: '#/components/schemas/AssertRefSnapshotId'
assert-last-assigned-field-id: '#/components/schemas/AssertLastAssignedFieldId
assert-current-schema-id: '#/components/schemas/AssertCurrentSchemaId'
assert-last-assigned-partition-id: '#/components/schemas/AssertLastAssignedPar
assert-default-spec-id: '#/components/schemas/AssertDefaultSpecId'
assert-default-sort-order-id: '#/components/schemas/AssertDefaultSortOrderId'
             */
        };
    }
}
