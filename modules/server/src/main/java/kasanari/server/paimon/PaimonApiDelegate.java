package kasanari.server.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.paimon.PaimonApi;
import kasanari.api.paimon.dto.*;

@ApplicationScoped
public class PaimonApiDelegate implements PaimonApi {
    @Override
    public PaimonAlterDatabaseResponseDto alterDatabase(String prefix, String database, PaimonAlterDatabaseRequestDto paimonAlterDatabaseRequestDto) {
        return null;
    }

    @Override
    public PaimonCreateDatabaseResponseDto createDatabases(String prefix, PaimonCreateDatabaseRequestDto paimonCreateDatabaseRequestDto) {
        return null;
    }

    @Override
    public void dropDatabase(String prefix, String database) {

    }

    @Override
    public PaimonConfigResponseDto getConfig() {
        return null;
    }

    @Override
    public PaimonGetDatabaseResponseDto getDatabases(String prefix, String database) {
        return null;
    }

    @Override
    public PaimonListDatabasesResponseDto listDatabases(String prefix) {
        return null;
    }
}
