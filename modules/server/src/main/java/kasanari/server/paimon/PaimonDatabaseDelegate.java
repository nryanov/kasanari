package kasanari.server.paimon;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.paimon.PaimonDatabaseApi;
import kasanari.api.paimon.dto.*;

@ApplicationScoped
public class PaimonDatabaseDelegate implements PaimonDatabaseApi {
    @Override
    public Uni<PaimonAlterDatabaseResponse> alterDatabase(String prefix, String database, PaimonAlterDatabaseRequest paimonAlterDatabaseRequest) {
        return null;
    }

    @Override
    public Uni<PaimonCreateDatabaseResponse> createDatabases(String prefix, PaimonCreateDatabaseRequest paimonCreateDatabaseRequest) {
        return null;
    }

    @Override
    public Uni<Void> dropDatabase(String prefix, String database) {
        return null;
    }

    @Override
    public Uni<PaimonGetDatabaseResponse> getDatabases(String prefix, String database) {
        return null;
    }

    @Override
    public Uni<PaimonListDatabasesResponse> listDatabases(String prefix) {
        return null;
    }
}
