package kasanari.catalog.iceberg.kasanari.repository.jdbc;

import kasanari.catalog.iceberg.kasanari.repository.NamespaceRepository;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.NamespaceNotEmptyException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.jdbi.v3.core.statement.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JdbcNamespaceRepository implements NamespaceRepository {
    private final KasanariDataSource dataSource;
    private final String catalogName;

    public JdbcNamespaceRepository(KasanariDataSource dataSource, String catalogName) {
        this.dataSource = dataSource;
        this.catalogName = catalogName;
    }

    @Override
    public void createNamespace(Namespace namespace, Map<String, String> metadata) {
        var namespaceName = String.join(".", namespace.levels());

        dataSource.getJdbi().useTransaction(tx -> {
            var checkIfNamespaceExistsQuery = tx.createQuery(JdbcQueries.CHECK_IF_NAMESPACE_EXISTS);
            checkIfNamespaceExistsQuery.bind(0, catalogName);
            checkIfNamespaceExistsQuery.bind(1, namespaceName);

            var exists = checkIfNamespaceExistsQuery.mapTo(Boolean.class);

            if (exists.first()) {
                throw new AlreadyExistsException(String.format("Namespace `%s` is already exists in catalog `%s`", namespaceName, catalogName));
            }

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
        });
    }

    @Override
    public List<Namespace> listNamespaces(Namespace namespace) throws NoSuchNamespaceException {
        var namespaceName = String.join(".", namespace.levels());

        return dataSource.getJdbi().inTransaction(tx -> {
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

            if (namespaces.isEmpty()) {
                throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
            }

            return namespaces;
        });
    }

    @Override
    public Map<String, String> loadNamespaceMetadata(Namespace namespace) throws NoSuchNamespaceException {
        var namespaceName = String.join(".", namespace.levels());

        return dataSource.getJdbi().inTransaction(tx -> {
            var checkIfNamespaceExistsQuery = tx.createQuery(JdbcQueries.CHECK_IF_NAMESPACE_EXISTS);
            checkIfNamespaceExistsQuery.bind(0, catalogName);
            checkIfNamespaceExistsQuery.bind(1, namespaceName);

            var exists = checkIfNamespaceExistsQuery.mapTo(Boolean.class);

            if (!exists.first()) {
                throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
            }

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
        });
    }

    @Override
    public boolean dropNamespace(Namespace namespace) throws NamespaceNotEmptyException {
        var namespaceName = String.join(".", namespace.levels());

        return dataSource.getJdbi().inTransaction(tx -> {
            var checkIfNamespaceExistsQuery = tx.createQuery(JdbcQueries.CHECK_IF_NAMESPACE_EXISTS);
            checkIfNamespaceExistsQuery.bind(0, catalogName);
            checkIfNamespaceExistsQuery.bind(1, namespaceName);

            var exists = checkIfNamespaceExistsQuery.mapTo(Boolean.class);

            if (!exists.first()) {
                return false;
            }

            var checkIfLinkedTablesExistQuery = tx.createQuery(JdbcQueries.CHECK_NAMESPACE_TABLES_RELATIONSHIPS);
            checkIfLinkedTablesExistQuery.bind(0, catalogName);
            checkIfLinkedTablesExistQuery.bind(1, namespaceName);

            var checkIfLinkedViewsExistQuery = tx.createQuery(JdbcQueries.CHECK_NAMESPACE_VIEWS_RELATIONSHIPS);
            checkIfLinkedViewsExistQuery.bind(0, catalogName);
            checkIfLinkedViewsExistQuery.bind(1, namespaceName);

            var linkedTablesExist = checkIfLinkedTablesExistQuery.mapTo(Boolean.class).first();
            var linkedViewsExist = checkIfLinkedViewsExistQuery.mapTo(Boolean.class).first();

            if (linkedTablesExist || linkedViewsExist) {
                throw new NamespaceNotEmptyException(String.format("Namespace `%s` in catalog `%s` cannot be dropped because it has linked entities", namespaceName, catalogName));
            }

            var dropNamespaceQuery = tx.createUpdate(JdbcQueries.DELETE_NAMESPACE);
            dropNamespaceQuery.bind(0, catalogName);
            dropNamespaceQuery.bind(1, namespaceName);
            dropNamespaceQuery.execute();

            return true;
        });
    }

    @Override
    public boolean setProperties(Namespace namespace, Map<String, String> properties) throws NoSuchNamespaceException {
        var namespaceName = String.join(".", namespace.levels());

        dataSource.getJdbi().useTransaction(tx -> {
            var checkIfNamespaceExistsQuery = tx.createQuery(JdbcQueries.CHECK_IF_NAMESPACE_EXISTS);
            checkIfNamespaceExistsQuery.bind(0, catalogName);
            checkIfNamespaceExistsQuery.bind(1, namespaceName);

            var exists = checkIfNamespaceExistsQuery.mapTo(Boolean.class);

            if (!exists.first()) {
                throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
            }

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
        });

        return true;
    }

    @Override
    public boolean removeProperties(Namespace namespace, Set<String> properties) throws NoSuchNamespaceException {
        var namespaceName = String.join(".", namespace.levels());

        dataSource.getJdbi().useTransaction(tx -> {
            var checkIfNamespaceExistsQuery = tx.createQuery(JdbcQueries.CHECK_IF_NAMESPACE_EXISTS);
            checkIfNamespaceExistsQuery.bind(0, catalogName);
            checkIfNamespaceExistsQuery.bind(1, namespaceName);

            var exists = checkIfNamespaceExistsQuery.mapTo(Boolean.class);

            if (!exists.first()) {
                throw new NoSuchNamespaceException(String.format("Namespace `%s` does not exist in catalog `%s`", namespaceName, catalogName));
            }

            var removeNamespacePropertiesQuery = tx.createUpdate(JdbcQueries.REMOVE_NAMESPACE_PROPERTIES);
            removeNamespacePropertiesQuery.bind(0, catalogName);
            removeNamespacePropertiesQuery.bind(1, namespaceName);
            removeNamespacePropertiesQuery.bind(2, properties.toArray(new String[0]));

            removeNamespacePropertiesQuery.execute();
        });

        return true;
    }
}
