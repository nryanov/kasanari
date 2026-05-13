package kasanari.catalog.lance.jdbc;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesSerde {
    private PropertiesSerde() {
    }

    public static String encode(Map<String, String> map) {
        var properties = new Properties();
        if (map != null) {
            properties.putAll(map);
        }

        try (var writer = new StringWriter()) {
            properties.store(writer, null);
            return writer.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, String> decode(String value) {
        if (value == null || value.isBlank()) {
            return new HashMap<>();
        }

        var properties = new Properties();
        try (var reader = new StringReader(value)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var result = new HashMap<String, String>();
        for (var entry : properties.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }
}
