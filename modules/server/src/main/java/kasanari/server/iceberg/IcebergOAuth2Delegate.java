package kasanari.server.iceberg;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.iceberg.IcebergOAuth2Api;
import kasanari.api.iceberg.dto.IcebergOAuthTokenResponse;
import kasanari.api.iceberg.dto.IcebergTokenType;

@ApplicationScoped
public class IcebergOAuth2Delegate implements IcebergOAuth2Api {
    @Override
    public Uni<IcebergOAuthTokenResponse> getToken(String grantType, String scope, String clientId, String clientSecret, IcebergTokenType requestedTokenType, String subjectToken, IcebergTokenType subjectTokenType, String actorToken, IcebergTokenType actorTokenType) {
        return null;
    }
}
