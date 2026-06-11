package kasanari.catalog.lance;

import org.apache.arrow.vector.types.DateUnit;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.lance.namespace.model.JsonArrowDataType;
import org.lance.namespace.model.JsonArrowField;
import org.lance.namespace.model.JsonArrowSchema;

import java.util.List;
import java.util.Locale;

public final class ArrowJsonSchemaMapper {
    private ArrowJsonSchemaMapper() {
    }

    public static JsonArrowSchema toJsonArrowSchema(Schema schema) {
        var response = new JsonArrowSchema().fields(mapFields(schema.getFields()));

        if (schema.getCustomMetadata() != null && !schema.getCustomMetadata().isEmpty()) {
            response.metadata(schema.getCustomMetadata());
        }

        return response;
    }

    private static List<JsonArrowField> mapFields(List<Field> fields) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }
        return fields.stream().map(ArrowJsonSchemaMapper::toJsonArrowField).toList();
    }

    private static JsonArrowField toJsonArrowField(Field field) {
        var response = new JsonArrowField()
                .name(field.getName())
                .nullable(field.isNullable())
                .type(toJsonArrowDataType(field.getType(), field.getChildren()));

        if (field.getMetadata() != null && !field.getMetadata().isEmpty()) {
            response.metadata(field.getMetadata());
        }

        return response;
    }

    private static JsonArrowDataType toJsonArrowDataType(ArrowType arrowType, List<Field> children) {
        var response = new JsonArrowDataType().type(typeName(arrowType));

        if (children != null && !children.isEmpty()) {
            response.fields(mapFields(children));
        }

        switch (arrowType) {
            case ArrowType.FixedSizeBinary fixedSizeBinary -> response.length((long) fixedSizeBinary.getByteWidth());
            case ArrowType.FixedSizeList fixedSizeList -> response.length((long) fixedSizeList.getListSize());
            default -> {}
        }

        return response;
    }

    private static String typeName(ArrowType arrowType) {
        return switch (arrowType) {
            case ArrowType.Int value -> (value.getIsSigned() ? "int" : "uint") + value.getBitWidth();
            case ArrowType.FloatingPoint value -> switch (value.getPrecision()) {
                case HALF -> "float16";
                case SINGLE -> "float32";
                case DOUBLE -> "float64";
            };
            case ArrowType.Bool ignored -> "bool";
            case ArrowType.Utf8 ignored -> "utf8";
            case ArrowType.LargeUtf8 ignored -> "large_utf8";
            case ArrowType.Binary ignored -> "binary";
            case ArrowType.LargeBinary ignored -> "large_binary";
            case ArrowType.FixedSizeBinary ignored -> "fixed_size_binary";
            case ArrowType.Decimal ignored -> "decimal";
            case ArrowType.Date value -> value.getUnit() == DateUnit.DAY ? "date32" : "date64";
            case ArrowType.Time value -> value.getBitWidth() == 32 ? "time32" : "time64";
            case ArrowType.Timestamp ignored -> "timestamp";
            case ArrowType.Duration ignored -> "duration";
            case ArrowType.Interval ignored -> "interval";
            case ArrowType.List ignored -> "list";
            case ArrowType.LargeList ignored -> "large_list";
            case ArrowType.FixedSizeList ignored -> "fixed_size_list";
            case ArrowType.Struct ignored -> "struct";
            case ArrowType.Map ignored -> "map";
            case ArrowType.Union ignored -> "union";
            case ArrowType.Null ignored -> "null";
            default -> arrowType.getTypeID().name().toLowerCase(Locale.ROOT);
        };
    }
}
