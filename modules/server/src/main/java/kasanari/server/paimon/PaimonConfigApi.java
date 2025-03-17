package kasanari.server.paimon;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.paimon.ConfigApi;
import kasanari.api.paimon.dto.PaimonConfigResponse;

@ApplicationScoped
public class PaimonConfigApi implements ConfigApi {
    @Override
    public Uni<PaimonConfigResponse> getConfig() {
        return null;
    }
}
