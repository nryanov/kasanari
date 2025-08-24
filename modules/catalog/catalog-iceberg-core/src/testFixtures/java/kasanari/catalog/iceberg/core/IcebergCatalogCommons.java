package kasanari.catalog.iceberg.core;


import org.apache.iceberg.Schema;
import org.apache.iceberg.SchemaParser;

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
}
