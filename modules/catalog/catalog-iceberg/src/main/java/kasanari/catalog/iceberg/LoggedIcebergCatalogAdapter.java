package kasanari.catalog.iceberg;

import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.GetNamespaceResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.LoadTableResponse;
import org.apache.iceberg.rest.responses.LoadViewResponse;
import org.apache.iceberg.rest.responses.UpdateNamespacePropertiesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoggedIcebergCatalogAdapter implements IcebergCatalogAdapter {
    private final static Logger logger = LoggerFactory.getLogger("IcebergCatalogAdapter");

    private final IcebergCatalogAdapter delegate;

    public LoggedIcebergCatalogAdapter(IcebergCatalogAdapter delegate) {
        this.delegate = delegate;
        MDC.put("catalog", delegate.delegate().name());
    }

    @Override
    public CreateNamespaceResponse createNamespace(Namespace namespace, Map<String, String> properties) {
        try {
            logger.info("Attempt to create namespace `{}`", namespace);
            var rs = delegate.createNamespace(namespace, properties);
            logger.info("Successfully created namespace `{}`", namespace);
            return rs;
        } catch (Exception e) {
            logger.error("Error happened while creating namespace `{}`: {}", namespace, e.getMessage());
            throw e;
        }
    }

    @Override
    public void dropNamespace(Namespace namespace) {
        try {
            logger.info("Attempt to drop namespace `{}`", namespace);
            delegate.dropNamespace(namespace);
            logger.info("Successfully dropped namespace `{}`", namespace);
        } catch (Exception e) {
            logger.error("Error happened while dropping namespace `{}`: {}", namespace, e.getMessage());
            throw e;
        }
    }

    @Override
    public GetNamespaceResponse loadNamespaceMetadata(Namespace namespace) {
        try {
            logger.info("Attempt to load namespace `{}`", namespace);
            var metadata = delegate.loadNamespaceMetadata(namespace);
            logger.info("Successfully loaded namespace `{}`", namespace);
            return metadata;
        } catch (Exception e) {
            logger.error("Error happened while loading namespace `{}`: {}", namespace, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean namespaceExists(Namespace namespace) {
        try {
            logger.info("Attempt to check if namespace `{}` exists", namespace);
            var exists = delegate.namespaceExists(namespace);
            logger.info("Successfully check existence of namespace `{}`", namespace);
            return exists;
        } catch (Exception e) {
            logger.error("Error happened while checking existence of namespace `{}`: {}", namespace, e.getMessage());
            throw e;
        }
    }

    @Override
    public ListNamespacesResponse listNamespaces(String pageToken, Integer pageSize, String parent) {
        try {
            logger.info("Attempt to list namespaces");
            var result = delegate.listNamespaces(pageToken, pageSize, parent);
            logger.info("Successfully listed namespaces");
            return result;
        } catch (Exception e) {
            logger.error("Error happened while listings existing namespaces: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public UpdateNamespacePropertiesResponse updateNamespace(Namespace namespace, Map<String, String> updates, Set<String> removals) {
        try {
            logger.info("Attempt to update namespace `{}`", namespace);
            var result = delegate.updateNamespace(namespace, updates, removals);
            logger.info("Successfully updated namespace `{}`", namespace);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while updating namespace `{}`: {}", namespace, e.getMessage());
            throw e;
        }
    }

    @Override
    public LoadViewResponse createView(Namespace namespace, CreateViewRequest rq) {
        var viewName = namespace.toString() + "." + rq.name();

        try {
            logger.info("Attempt to create view `{}`", viewName);
            var result = delegate.createView(namespace, rq);
            logger.info("Successfully created view `{}`", viewName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while creating view `{}`: {}", viewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean viewExists(TableIdentifier view) {
        var viewName = view.toString();

        try {
            logger.info("Attempt to check if view `{}` exists", viewName);
            var exists = delegate.viewExists(view);
            logger.info("Successfully checked that view `{}` exists", viewName);
            return exists;
        } catch (Exception e) {
            logger.error("Error happened while checking existence of view `{}`: {}", viewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public LoadViewResponse loadView(TableIdentifier view) {
        var viewName = view.toString();

        try {
            logger.info("Attempt to load view `{}`", viewName);
            var result = delegate.loadView(view);
            logger.info("Successfully loaded view `{}`", viewName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while loading view `{}`: {}", viewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void renameView(TableIdentifier from, TableIdentifier to) {
        var fromViewName = from.toString();
        var toViewName = to.toString();

        try {
            logger.info("Attempt to rename view from `{}` to `{}`", fromViewName, toViewName);
            delegate.renameView(from, to);
            logger.info("Successfully renamed view from `{}` to `{}`", fromViewName, toViewName);
        } catch (Exception e) {
            logger.error("Error happened while renaming view from `{}` to `{}`: {}", fromViewName, toViewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public ListTablesResponse listViews(Namespace namespace, String pageToken, Integer pageSize) {
        var namespaceName = namespace.toString();

        try {
            logger.info("Attempt to list views from namespace `{}`", namespaceName);
            var result = delegate.listViews(namespace, pageToken, pageSize);
            logger.info("Successfully listed views from namespace `{}`", namespaceName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while listed views from namespace `{}`: {}", namespaceName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void dropView(TableIdentifier view) {
        var viewName = view.toString();

        try {
            logger.info("Attempt to drop view `{}`", viewName);
            delegate.dropView(view);
            logger.info("Successfully dropped view `{}`", viewName);
        } catch (Exception e) {
            logger.error("Error happened while dropping view `{}`: {}", viewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public LoadViewResponse replaceView(TableIdentifier view, UpdateTableRequest rq) {
        var viewName = view.toString();

        try {
            logger.info("Attempt to replace view `{}`", viewName);
            var result = delegate.replaceView(view, rq);
            logger.info("Successfully replaced view `{}`", viewName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while replacing view `{}`: {}", viewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean tableExists(TableIdentifier table) {
        var tableName = table.toString();

        try {
            logger.info("Attempt to check if table exists `{}`", tableName);
            var result = delegate.tableExists(table);
            logger.info("Successfully checked existence of table `{}`", tableName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while checking existence of table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void dropTable(TableIdentifier table, boolean purge) {
        var tableName = table.toString();

        try {
            logger.info("Attempt to drop table `{}`", tableName);
            delegate.dropTable(table, purge);
            logger.info("Successfully dropped table `{}`", tableName);
        } catch (Exception e) {
            logger.error("Error happened while dropping table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public ListTablesResponse listTables(Namespace namespace, String pageToken, Integer pageSize) {
        var namespaceName = namespace.toString();

        try {
            logger.info("Attempt to list tables in namespace `{}`", namespaceName);
            var result = delegate.listTables(namespace, pageToken, pageSize);
            logger.info("Successfully listed tables in namespace `{}`", namespaceName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while listing tables in namespace `{}`: {}", namespaceName, e.getMessage());
            throw e;
        }
    }

    @Override
    public LoadTableResponse createTable(Namespace namespace, CreateTableRequest rq) {
        var tableName = namespace.toString() + "." + rq.name();

        try {
            logger.info("Attempt to create table `{}`", tableName);
            var result = delegate.createTable(namespace, rq);
            logger.info("Successfully created table `{}`", tableName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while creating table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void renameTable(TableIdentifier from, TableIdentifier to) {
        var tableNameFrom = from.toString();
        var tableNameTo = to.toString();

        try {
            logger.info("Attempt to rename table from `{}` to `{}`", tableNameFrom, tableNameTo);
            delegate.renameTable(from, to);
            logger.info("Successfully renamed table from `{}` to `{}`", tableNameFrom, tableNameTo);
        } catch (Exception e) {
            logger.error("Error happened while renaming table from `{}` to `{}`", tableNameFrom, tableNameTo);
            throw e;
        }
    }

    @Override
    public LoadTableResponse registerTable(TableIdentifier table, String location) {
        var tableName = table.toString();

        try {
            logger.info("Attempt to register table `{}`", tableName);
            var result = delegate.registerTable(table, location);
            logger.info("Successfully registered table `{}`", tableName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while registering table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public LoadTableResponse updateTable(TableIdentifier table, UpdateTableRequest rq) {
        var tableName = table.toString();

        try {
            logger.info("Attempt to update table `{}`", tableName);
            var result = delegate.updateTable(table, rq);
            logger.info("Successfully updated table `{}`", tableName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while updating table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public LoadTableResponse loadTable(TableIdentifier table) {
        var tableName = table.toString();

        try {
            logger.info("Attempt to load table `{}`", tableName);
            var result = delegate.loadTable(table);
            logger.info("Successfully loaded table `{}`", tableName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while loading table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void commitTransaction(List<UpdateTableRequest> transactions) {
        try {
            logger.info("Attempt to commit transactions");
            delegate.commitTransaction(transactions);
            logger.info("Successfully committed transactions");
        } catch (Exception e) {
            logger.error("Error happened while commiting transactions: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Catalog delegate() {
        return delegate.delegate();
    }
}
