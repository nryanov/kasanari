package kasanari.repository.iceberg.postgres;

import kasanari.repository.iceberg.NamespaceRepository;
import kasanari.repository.iceberg.IcebergUtils;
import org.apache.iceberg.catalog.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JdbcNamespaceRepository implements NamespaceRepository<Handle> {
    private final String catalogName;

    public JdbcNamespaceRepository(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public void create(Handle tx, Namespace namespace, Map<String, String> metadata) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var createNamespaceQuery = tx.createUpdate(JdbcQueries.CREATE_NAMESPACE);
        createNamespaceQuery.bind(0, catalogName);
        createNamespaceQuery.bind(1, namespaceName);

        createNamespaceQuery.execute();

        var upsertNamespacePropertiesBatch = tx.prepareBatch(JdbcQueries.UPSERT_NAMESPACE_PROPERTIES);
        metadata.forEach((key, value) -> {
            upsertNamespacePropertiesBatch
                    .bind(0, catalogName)
                    .bind(1, namespaceName)
                    .bind(2, key)
                    .bind(3, value)
                    .add();
        });

        upsertNamespacePropertiesBatch.execute();
    }

    @Override
    public List<Namespace> list(Handle tx, Namespace namespace) {
        var namespaces = new ArrayList<Namespace>();

        Query query;
        if (namespace.isEmpty()) {
            query = tx.createQuery(JdbcQueries.SELECT_ROOT_NAMESPACES);
            query.bind(0, catalogName);
        } else {
            query = tx.createQuery(JdbcQueries.SELECT_CHILD_NAMESPACES);
            query.bind(0, catalogName);
            query.bind(1, String.format("^%s[.][^.]+$", String.join("[.]", namespace.levels())));
        }

        query
                .mapTo(String.class)
                .forEach(it -> namespaces.add(Namespace.of(it.split("[.]"))));

        return namespaces;
    }

    @Override
    public Map<String, String> load(Handle tx, Namespace namespace) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var selectNamespacePropertiesQuery = tx.createQuery(JdbcQueries.SELECT_NAMESPACE_PROPERTIES);
        selectNamespacePropertiesQuery.bind(0, catalogName);
        selectNamespacePropertiesQuery.bind(1, namespaceName);

        var result = new HashMap<String, String>();
        var propertiesValues = selectNamespacePropertiesQuery.mapToMap();
        propertiesValues.forEach(row -> {
            var key = row.get("property_key").toString();
            var value = row.get("property_value").toString();
            result.put(key, value);
        });

        return result;
    }

    @Override
    public boolean delete(Handle tx, Namespace namespace) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var dropNamespaceQuery = tx.createUpdate(JdbcQueries.DELETE_NAMESPACE);
        dropNamespaceQuery.bind(0, catalogName);
        dropNamespaceQuery.bind(1, namespaceName);
        var affectedRows = dropNamespaceQuery.execute();

        return affectedRows == 1;
    }

    @Override
    public boolean setProperties(Handle tx, Namespace namespace, Map<String, String> properties) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var upsertNamespacePropertiesBatch = tx.prepareBatch(JdbcQueries.UPSERT_NAMESPACE_PROPERTIES);
        properties.forEach((key, value) -> {
            upsertNamespacePropertiesBatch
                    .bind(0, catalogName)
                    .bind(1, namespaceName)
                    .bind(2, key)
                    .bind(3, value)
                    .add();
        });

        upsertNamespacePropertiesBatch.execute();

        return true;
    }

    @Override
    public boolean removeProperties(Handle tx, Namespace namespace, Set<String> properties) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var removeNamespacePropertiesQuery = tx.createUpdate(JdbcQueries.REMOVE_NAMESPACE_PROPERTIES);
        removeNamespacePropertiesQuery.bind(0, catalogName);
        removeNamespacePropertiesQuery.bind(1, namespaceName);
        removeNamespacePropertiesQuery.bind(2, properties.toArray(new String[0]));

        removeNamespacePropertiesQuery.execute();

        return true;
    }

    @Override
    public boolean exists(Handle tx, Namespace namespace) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var checkIfNamespaceExistsQuery = tx.createQuery(JdbcQueries.CHECK_IF_NAMESPACE_EXISTS);
        checkIfNamespaceExistsQuery.bind(0, catalogName);
        checkIfNamespaceExistsQuery.bind(1, namespaceName);

        return checkIfNamespaceExistsQuery.mapTo(Boolean.class).first();
    }

    @Override
    public boolean linkedTablesExist(Handle tx, Namespace namespace) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var query = tx.createQuery(JdbcQueries.CHECK_NAMESPACE_TABLES_RELATIONSHIPS);
        query.bind(0, catalogName);
        query.bind(1, namespaceName);

        return query.mapTo(Boolean.class).first();
    }

    @Override
    public boolean linkedViewsExist(Handle tx, Namespace namespace) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var query = tx.createQuery(JdbcQueries.CHECK_NAMESPACE_VIEWS_RELATIONSHIPS);
        query.bind(0, catalogName);
        query.bind(1, namespaceName);

        return query.mapTo(Boolean.class).first();
    }
}
