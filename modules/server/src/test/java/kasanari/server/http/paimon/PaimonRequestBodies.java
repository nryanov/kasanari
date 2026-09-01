package kasanari.server.http.paimon;

import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.types.DataField;
import org.apache.paimon.types.DataTypes;
import org.apache.paimon.view.ViewSchema;

import java.util.Collections;
import java.util.List;
import java.util.Map;

final class PaimonRequestBodies {
    private PaimonRequestBodies() {}

    static Schema tableSchema() {
        return new Schema(
                List.of(new DataField(0, "id", DataTypes.INT())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap(),
                "test"
        );
    }

    static ViewSchema viewSchema() {
        return new ViewSchema(
                List.of(new DataField(0, "id", DataTypes.INT())),
                "SELECT 1",
                Collections.emptyMap(),
                "test view",
                Collections.emptyMap()
        );
    }

    static Identifier tableId(String database, String table) {
        return Identifier.create(database, table);
    }
}
