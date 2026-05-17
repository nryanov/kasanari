package kasanari.catalog.lance;

import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.List;

/**
 * Builds minimal Arrow IPC stream payloads for Lance namespace table data operations.
 */
final class LanceArrowIpc {
    static final Schema TABLE_SCHEMA = new Schema(List.of(
            Field.nullable("id", new ArrowType.Int(64, true)),
            Field.nullable("col_a", new ArrowType.Utf8())
    ));

    private LanceArrowIpc() {
    }

    static byte[] emptyBatch() {
        return writeBatch(0);
    }

    static byte[] singleRowBatch() {
        return writeBatch(1);
    }

    private static byte[] writeBatch(int rowCount) {
        try (var allocator = new RootAllocator();
             var root = VectorSchemaRoot.create(TABLE_SCHEMA, allocator)) {
            var idVector = (BigIntVector) root.getVector("id");
            var colAVector = (VarCharVector) root.getVector("col_a");
            idVector.allocateNew(rowCount);
            colAVector.allocateNew(rowCount);
            for (var i = 0; i < rowCount; i++) {
                idVector.set(i, i + 1L);
                colAVector.setSafe(i, ("row-" + i).getBytes());
            }
            idVector.setValueCount(rowCount);
            colAVector.setValueCount(rowCount);
            root.setRowCount(rowCount);
            return toIpcStream(root);
        }
    }

    private static byte[] toIpcStream(VectorSchemaRoot root) {
        var out = new ByteArrayOutputStream();
        try (var writer = new ArrowStreamWriter(root, null, Channels.newChannel(out))) {
            writer.start();
            writer.writeBatch();
            writer.end();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write Arrow IPC stream", e);
        }
        return out.toByteArray();
    }
}
