package kasanari.catalog.iceberg.repository;

import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.exceptions.NamespaceNotEmptyException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NamespaceRepository<T> {
    void create(T tx, Namespace namespace, Map<String, String> metadata);

    List<Namespace> list(T tx, Namespace namespace) throws NoSuchNamespaceException;

    Map<String, String> load(T tx, Namespace namespace) throws NoSuchNamespaceException;

    boolean delete(T tx, Namespace namespace) throws NamespaceNotEmptyException;

    boolean setProperties(T tx, Namespace namespace, Map<String, String> properties) throws NoSuchNamespaceException;

    boolean removeProperties(T tx, Namespace namespace, Set<String> properties) throws NoSuchNamespaceException;

    boolean exists(T tx, Namespace namespace);

    default boolean notExists(T tx, Namespace namespace) {
        return !exists(tx, namespace);
    }

    boolean linkedTablesExist(T tx, Namespace namespace);

    boolean linkedViewsExist(T tx, Namespace namespace);
}
