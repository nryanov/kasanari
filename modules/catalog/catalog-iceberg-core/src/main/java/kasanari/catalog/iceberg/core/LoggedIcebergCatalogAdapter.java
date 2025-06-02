package kasanari.catalog.iceberg.core;

import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergTable;
import kasanari.catalog.iceberg.core.model.IcebergValues;
import kasanari.catalog.iceberg.core.model.IcebergView;
import org.apache.iceberg.catalog.Catalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;

public class LoggedIcebergCatalogAdapter implements IcebergCatalogAdapter {
    private final static Logger logger = LoggerFactory.getLogger("IcebergCatalogAdapter");

    private final IcebergCatalogAdapter delegate;

    public LoggedIcebergCatalogAdapter(IcebergCatalogAdapter delegate) {
        this.delegate = delegate;
        MDC.put("catalog", delegate.delegate().name());
    }

    @Override
    public void createNamespace(IcebergNamespace namespace) {
        try {
            logger.info("Attempt to create namespace `{}`", namespace.name().pretty());
            delegate.createNamespace(namespace);
            logger.info("Successfully created namespace `{}`", namespace.name().pretty());
        } catch (Exception e) {
            logger.error("Error happened while creating namespace `{}`: {}", namespace.name().pretty(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void dropNamespace(IcebergNamespace.Name namespace) {
        try {
            logger.info("Attempt to drop namespace `{}`", namespace.pretty());
            delegate.dropNamespace(namespace);
            logger.info("Successfully dropped namespace `{}`", namespace.pretty());
        } catch (Exception e) {
            logger.error("Error happened while dropping namespace `{}`: {}", namespace.pretty(), e.getMessage());
            throw e;
        }
    }

    @Override
    public IcebergNamespace loadNamespaceMetadata(IcebergNamespace.Name namespace) {
        try {
            logger.info("Attempt to load namespace `{}`", namespace.pretty());
            var metadata = delegate.loadNamespaceMetadata(namespace);
            logger.info("Successfully loaded namespace `{}`", namespace.pretty());
            return metadata;
        } catch (Exception e) {
            logger.error("Error happened while loading namespace `{}`: {}", namespace.pretty(), e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean namespaceExists(IcebergNamespace.Name namespace) {
        try {
            logger.info("Attempt to check if namespace `{}` exists", namespace.pretty());
            var exists = delegate.namespaceExists(namespace);
            logger.info("Successfully check existence of namespace `{}`", namespace.pretty());
            return exists;
        } catch (Exception e) {
            logger.error("Error happened while checking existence of namespace `{}`: {}", namespace.pretty(), e.getMessage());
            throw e;
        }
    }

    @Override
    public IcebergNamespace.Listing listNamespaces(IcebergNamespace.Listing.Filter filter) {
        try {
            logger.info("Attempt to list namespaces");
            var result = delegate.listNamespaces(filter);
            logger.info("Successfully listed namespaces");
            return result;
        } catch (Exception e) {
            logger.error("Error happened while listings existing namespaces: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public IcebergNamespace updateNamespace(IcebergNamespace.Name namespace, IcebergNamespace.Update rq) {
        try {
            logger.info("Attempt to update namespace `{}`", namespace.pretty());
            var result = delegate.updateNamespace(namespace, rq);
            logger.info("Successfully updated namespace `{}`", namespace.pretty());
            return result;
        } catch (Exception e) {
            logger.error("Error happened while updating namespace `{}`: {}", namespace.pretty(), e.getMessage());
            throw e;
        }
    }

    @Override
    public IcebergView.Metadata createView(IcebergView.CreateRequest createRq) {
        var viewName = namespaceName(createRq.namespace()) + createRq.name().value();

        try {
            logger.info("Attempt to create view `{}`", viewName);
            var result = delegate.createView(createRq);
            logger.info("Successfully created view `{}`", viewName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while creating view `{}`: {}", viewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean viewExists(IcebergNamespace.Name namespace, IcebergView.Name view) {
        var viewName = namespaceName(namespace) + view.value();

        try {
            logger.info("Attempt to check if view `{}` exists", viewName);
            var exists = delegate.viewExists(namespace, view);
            logger.info("Successfully checked that view `{}` exists", viewName);
            return exists;
        } catch (Exception e) {
            logger.error("Error happened while checking existence of view `{}`: {}", viewName, e.getMessage());
            throw e;
        }
    }

    @Override
    public IcebergView.Metadata loadView(IcebergView view) {
        var viewName = namespaceName(view.namespace()) + view.name().value();

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
    public void renameView(IcebergView from, IcebergView to) {
        var fromViewName = namespaceName(from.namespace()) + from.name().value();
        var toViewName = namespaceName(to.namespace()) + to.name().value();

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
    public IcebergView.Listing listViews(IcebergNamespace.Name namespace, IcebergView.Listing.Filter filter) {
        var namespaceName = namespace.pretty();

        try {
            logger.info("Attempt to list views from namespace `{}`", namespaceName);
            var result = delegate.listViews(namespace, filter);
            logger.info("Successfully listed views from namespace `{}`", namespaceName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while listed views from namespace `{}`: {}", namespaceName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void dropView(IcebergView view) {
        var viewName = namespaceName(view.namespace()) + view.name().value();

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
    public IcebergView.Metadata replaceView(IcebergView view, IcebergView.UpdateRequest rq) {
        var viewName = namespaceName(view.namespace()) + view.name().value();

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
    public boolean tableExists(IcebergNamespace.Name namespace, IcebergTable.Name name) {
        var tableName = namespaceName(namespace) + name.value();

        try {
            logger.info("Attempt to check if table exists `{}`", tableName);
            var result = delegate.tableExists(namespace, name);
            logger.info("Successfully checked existence of table `{}`", tableName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while checking existence of table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void dropTable(IcebergTable table, boolean purge) {
        var tableName = namespaceName(table.namespace()) + table.name().value();

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
    public IcebergTable.Listing listTables(IcebergNamespace.Name namespace, IcebergTable.Listing.Filter filter) {
        var namespaceName = namespaceName(namespace);

        try {
            logger.info("Attempt to list tables in namespace `{}`", namespaceName);
            var result = delegate.listTables(namespace, filter);
            logger.info("Successfully listed tables in namespace `{}`", namespaceName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while listing tables in namespace `{}`: {}", namespaceName, e.getMessage());
            throw e;
        }
    }

    @Override
    public IcebergTable.LoadedTable createTable(IcebergTable.CreateRequest rq) {
        var tableName = namespaceName(rq.namespace()) + rq.name().value();

        try {
            logger.info("Attempt to create table `{}`", tableName);
            var result = delegate.createTable(rq);
            logger.info("Successfully created table `{}`", tableName);
            return result;
        } catch (Exception e) {
            logger.error("Error happened while creating table `{}`: {}", tableName, e.getMessage());
            throw e;
        }
    }

    @Override
    public void renameTable(IcebergTable from, IcebergTable to) {
        var tableNameFrom = namespaceName(from.namespace()) + from.name().value();
        var tableNameTo = namespaceName(to.namespace()) + to.name().value();

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
    public IcebergTable.LoadedTable registerTable(IcebergTable table, IcebergValues.Location location) {
        var tableName = namespaceName(table.namespace()) + table.name().value();

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
    public IcebergTable.Commit updateTable(IcebergTable table, IcebergTable.UpdateRequest rq) {
        var tableName = namespaceName(table.namespace()) + table.name().value();

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
    public IcebergTable.LoadedTable loadTable(IcebergTable table) {
        var tableName = namespaceName(table.namespace()) + table.name().value();

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
    public void commitTransaction(List<IcebergTable.Transaction> transactions) {
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

    private String namespaceName(IcebergNamespace.Name name) {
        if (name.levels().length == 0) {
            return "";
        }

        return name.pretty() + ".";
    }
}
