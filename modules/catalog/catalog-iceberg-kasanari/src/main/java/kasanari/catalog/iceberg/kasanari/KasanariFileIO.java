package kasanari.catalog.iceberg.kasanari;

import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFile;

import java.util.Map;

public class KasanariFileIO implements FileIO {
    @Override
    public InputFile newInputFile(String path) {
        return null;
    }

    @Override
    public OutputFile newOutputFile(String path) {
        return null;
    }

    @Override
    public void deleteFile(String path) {

    }

    @Override
    public void initialize(Map<String, String> properties) {
        FileIO.super.initialize(properties);
    }
}
