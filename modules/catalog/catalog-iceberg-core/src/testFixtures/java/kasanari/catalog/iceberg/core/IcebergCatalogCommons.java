package kasanari.catalog.iceberg.core;


import org.apache.iceberg.Schema;
import org.apache.iceberg.SchemaParser;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.ImmutableCreateViewRequest;
import org.apache.iceberg.view.ImmutableSQLViewRepresentation;
import org.apache.iceberg.view.ImmutableViewVersion;

import java.util.List;
import java.util.Map;

public class IcebergCatalogCommons {
    public static final String DEFAULT_SCHEMA_JSON = """
                                {
                  "type": "struct",
                  "fields": [
                    {
                      "id": 1,
                      "name": "id",
                      "type": "long",
                      "required": true
                    }
                  ]
                }
                """;

    public static final Schema DEFAULT_SCHEMA = SchemaParser.fromJson(DEFAULT_SCHEMA_JSON);

    public static CreateViewRequest defaultCreateViewRequest(String name) {
        return ImmutableCreateViewRequest
                .builder()
                .name(name)
                .location("location")
                .schema(IcebergCatalogCommons.DEFAULT_SCHEMA)
                .viewVersion(
                        ImmutableViewVersion
                                .builder()
                                .versionId(1)
                                .timestampMillis(1)
                                .schemaId(1)
                                .putAllSummary(Map.of())
                                .addAllRepresentations(
                                        List.of(
                                                ImmutableSQLViewRepresentation
                                                        .builder()
                                                        .dialect("spark")
                                                        .sql("select * from table")
                                                        .build()
                                        )
                                )
                                .build()
                )
                .build();
    }
}
