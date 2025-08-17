//package kasanari.catalog.iceberg.core;
//
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//public class IcebergCatalogCommons {
//    public static final String DEFAULT_SCHEMA = """
//                                {
//                  "type": "struct",
//                  "fields": [
//                    {
//                      "id": 1,
//                      "name": "id",
//                      "type": "long",
//                      "required": true
//                    }
//                  ]
//                }
//                """;
//
//    public static IcebergView.CreateRequest defaultCreateViewRequest(
//            IcebergNamespace.Name namespaceName,
//            IcebergView.Name viewName
//    ) {
//        return new IcebergView.CreateRequest(
//                namespaceName,
//                viewName,
//                new IcebergValues.Location("location"),
//                new IcebergValues.Schema(DEFAULT_SCHEMA),
//                new IcebergView.Metadata.Version(
//                        new IcebergValues.VersionId(1),
//                        new IcebergValues.Timestamp(1L),
//                        new IcebergValues.SchemaId(1),
//                        Map.of(),
//                        List.of(
//                                new IcebergView.Metadata.Version.Representation(
//                                        new IcebergView.Metadata.Version.Representation.Type("sql"),
//                                        new IcebergView.Metadata.Version.Representation.Sql("select * from table"),
//                                        new IcebergView.Metadata.Version.Representation.Dialect("sql")
//                                )
//                        ),
//                        Optional.empty(),
//                        namespaceName
//                ),
//                Map.of()
//        );
//    }
//}
