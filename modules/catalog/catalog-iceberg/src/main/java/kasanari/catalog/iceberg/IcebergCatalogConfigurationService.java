package kasanari.catalog.iceberg;

import org.apache.iceberg.rest.Endpoint;
import org.apache.iceberg.rest.responses.ConfigResponse;

import java.util.List;

public class IcebergCatalogConfigurationService {
    private final List<Endpoint> endpoints;

    public IcebergCatalogConfigurationService() {
        endpoints = List.of(
                // namespace
                Endpoint.V1_CREATE_NAMESPACE,
                Endpoint.V1_DELETE_NAMESPACE,
                Endpoint.V1_LIST_NAMESPACES,
                Endpoint.V1_LOAD_NAMESPACE,
                Endpoint.V1_NAMESPACE_EXISTS,
                Endpoint.V1_UPDATE_NAMESPACE,
                // table
                Endpoint.V1_CREATE_TABLE,
                Endpoint.V1_DELETE_TABLE,
                Endpoint.V1_LIST_TABLES,
                Endpoint.V1_LOAD_TABLE,
                Endpoint.V1_REGISTER_TABLE,
                Endpoint.V1_RENAME_TABLE,
                Endpoint.V1_UPDATE_TABLE,
                Endpoint.V1_TABLE_EXISTS,
                // view
                Endpoint.V1_CREATE_VIEW,
                Endpoint.V1_DELETE_VIEW,
                Endpoint.V1_LIST_VIEWS,
                Endpoint.V1_LOAD_VIEW,
                Endpoint.V1_UPDATE_VIEW,
                Endpoint.V1_RENAME_VIEW,
                Endpoint.V1_VIEW_EXISTS,
                // transactions
                Endpoint.V1_COMMIT_TRANSACTION,
                // other
                Endpoint.V1_REPORT_METRICS
        );
    }

    public ConfigResponse getConfig(String warehouse) {
        return ConfigResponse
                .builder()
                .withEndpoints(endpoints)
                .withOverride("prefix", warehouse)
                .build();
    }
}
