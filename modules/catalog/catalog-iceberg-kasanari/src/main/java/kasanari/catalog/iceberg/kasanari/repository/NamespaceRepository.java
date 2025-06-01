package kasanari.catalog.iceberg.kasanari.repository;

import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.exceptions.NamespaceNotEmptyException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NamespaceRepository {
    void create(Namespace namespace, Map<String, String> metadata);

    List<Namespace> list(Namespace namespace) throws NoSuchNamespaceException;

    Map<String, String> load(Namespace namespace) throws NoSuchNamespaceException;

    boolean delete(Namespace namespace) throws NamespaceNotEmptyException;

    boolean setProperties(Namespace namespace, Map<String, String> properties) throws NoSuchNamespaceException;

    boolean removeProperties(Namespace namespace, Set<String> properties) throws NoSuchNamespaceException;

    boolean exists(Namespace namespace);

    default boolean notExist(Namespace namespace) {
        return !exists(namespace);
    }
}
