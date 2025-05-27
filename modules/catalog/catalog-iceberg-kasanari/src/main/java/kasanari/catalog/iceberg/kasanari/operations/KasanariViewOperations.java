package kasanari.catalog.iceberg.kasanari.operations;

import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.view.BaseViewOperations;
import org.apache.iceberg.view.ViewMetadata;

public class KasanariViewOperations extends BaseViewOperations {
    @Override
    protected void doRefresh() {

    }

    @Override
    protected void doCommit(ViewMetadata base, ViewMetadata metadata) {

    }

    @Override
    protected String viewName() {
        return "";
    }

    @Override
    protected FileIO io() {
        return null;
    }
}
