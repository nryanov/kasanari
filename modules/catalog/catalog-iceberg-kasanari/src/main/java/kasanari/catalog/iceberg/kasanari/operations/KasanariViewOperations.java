package kasanari.catalog.iceberg.kasanari.operations;

import kasanari.catalog.iceberg.kasanari.repository.NamespaceRepository;
import kasanari.catalog.iceberg.kasanari.repository.TableRepository;
import kasanari.catalog.iceberg.kasanari.repository.ViewRepository;
import kasanari.catalog.iceberg.kasanari.repository.model.IcebergViewRecord;
import kasanari.catalog.iceberg.kasanari.utils.IcebergUtils;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.exceptions.NoSuchNamespaceException;
import org.apache.iceberg.exceptions.NoSuchViewException;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.view.BaseViewOperations;
import org.apache.iceberg.view.ViewMetadata;

import java.util.Objects;

public class KasanariViewOperations extends BaseViewOperations {
    private final NamespaceRepository namespaceRepository;
    private final TableRepository tableRepository;
    private final ViewRepository viewRepository;
    private final FileIO fileIO;
    private final TableIdentifier viewIdentifier;
    private final String catalogName;

    public KasanariViewOperations(
            NamespaceRepository namespaceRepository,
            TableRepository tableRepository,
            ViewRepository viewRepository,
            FileIO fileIO,
            TableIdentifier viewIdentifier,
            String catalogName
    ) {
        this.namespaceRepository = namespaceRepository;
        this.tableRepository = tableRepository;
        this.viewRepository = viewRepository;
        this.fileIO = fileIO;
        this.viewIdentifier = viewIdentifier;
        this.catalogName = catalogName;
    }

    @Override
    protected void doRefresh() {
        if (viewRepository.exists(viewIdentifier)) {
            var view = viewRepository.load(viewIdentifier);

            if (view.metadataLocation() == null) {
                throw new ValidationException("State of view `%s` is incorrect: metadata location is null", viewIdentifier);
            }

            refreshFromMetadataLocation(view.metadataLocation());
        } else {
            // if table does not exist but there is a metadata info
            if (currentMetadataLocation() != null) {
                throw new NoSuchViewException(
                        "View `%s` doesn't exist in catalog `%s`",
                        viewIdentifier.toString(), catalogName
                );
            } else {
                // table does not exist and there is no existing metadata
                disableRefresh();
            }
        }
    }

    @Override
    protected void doCommit(ViewMetadata base, ViewMetadata metadata) {
        var isNewView = base == null;
        var newMetadataLocation = writeNewMetadataIfRequired(metadata);

        var failure = false;
        try {
            if (isNewView) {
                createView(newMetadataLocation);
            } else {
                var existingView = viewRepository.load(viewIdentifier);
                // check that current location didn't change yet
                validateMetadataLocation(existingView, base);
                updateView(existingView.metadataLocation(), newMetadataLocation);
            }
        } catch (Exception e) {
            failure = true;
            // todo: log & throw error
        } finally {
            if (failure) {
                fileIO.deleteFile(newMetadataLocation);
            }
        }
    }

    private void validateMetadataLocation(IcebergViewRecord view, ViewMetadata base) {
        String catalogMetadataLocation = view.metadataLocation();
        String baseMetadataLocation = base != null ? base.metadataFileLocation() : null;

        if (!Objects.equals(baseMetadataLocation, catalogMetadataLocation)) {
            throw new CommitFailedException(
                    "Cannot commit %s: metadata location %s has changed from %s",
                    viewIdentifier, baseMetadataLocation, catalogMetadataLocation);
        }
    }

    private void createView(String newMetadataLocation) {
        var namespaceName = IcebergUtils.namespaceName(viewIdentifier.namespace());

        if (!namespaceRepository.exists(viewIdentifier.namespace())) {
            throw new NoSuchNamespaceException(
                    "View couldn't be created because namespace `%s` does not exist in catalog `%s`",
                    namespaceName,
                    catalogName
            );
        }

        if (viewRepository.exists(viewIdentifier)) {
            throw new AlreadyExistsException(
                    "View couldn't be created because view with the same name `%s` is already exist in catalog `%s`",
                    viewIdentifier.toString(),
                    catalogName
            );
        }

        if (tableRepository.exists(viewIdentifier)) {
            throw new AlreadyExistsException(
                    "View couldn't be created because table with the same name `%s` is already exist in catalog `%s`",
                    viewIdentifier.toString(),
                    catalogName
            );
        }

        var result = viewRepository.create(viewIdentifier, newMetadataLocation);

        if (!result) {
            throw new CommitFailedException(
                    "View `%s` wasn't created in catalog `%s`",
                    viewIdentifier,
                    catalogName
            );
        }
    }

    private void updateView(String previousMetadataLocation, String newMetadataLocation) {
        var result = viewRepository.update(viewIdentifier, previousMetadataLocation, newMetadataLocation);

        if (!result) {
            throw new CommitFailedException(
                    "View `%s` wasn't updated in catalog `%s`",
                    viewIdentifier.toString(),
                    catalogName
            );
        }
    }

    @Override
    protected String viewName() {
        return viewIdentifier.toString();
    }

    @Override
    protected FileIO io() {
        return fileIO;
    }
}
