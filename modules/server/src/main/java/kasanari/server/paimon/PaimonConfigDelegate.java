package kasanari.server.paimon;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.paimon.PaimonConfigApi;
import kasanari.api.paimon.dto.PaimonConfigResponse;

@ApplicationScoped
public class PaimonConfigDelegate implements PaimonConfigApi {
    @Override
    public Uni<PaimonConfigResponse> getConfig() {
        return null;
    }
}
