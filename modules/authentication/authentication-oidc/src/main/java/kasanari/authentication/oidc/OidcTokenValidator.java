package kasanari.authentication.oidc;

import io.quarkus.oidc.common.runtime.OidcCommonUtils;
import io.vertx.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.jwk.HttpsJwks;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;

final class OidcTokenValidator implements Closeable {
    private final Vertx vertx;
    private final JwtConsumer jwtConsumer;
    private final String issuerUrl;
    private final Optional<String> clientId;

    private OidcTokenValidator(Vertx vertx, JwtConsumer jwtConsumer, String issuerUrl, Optional<String> clientId) {
        this.vertx = vertx;
        this.jwtConsumer = jwtConsumer;
        this.issuerUrl = issuerUrl;
        this.clientId = clientId;
    }

    static OidcTokenValidator create(String issuerUrl, Optional<String> clientId) {
        var vertx = Vertx.vertx();
        var mutinyVertx = io.vertx.mutiny.core.Vertx.newInstance(vertx);
        var webClient = WebClient.create(mutinyVertx);

        try {
            var discoveryUrl = trimTrailingSlash(issuerUrl);
            var metadata = OidcCommonUtils.discoverMetadata(
                            webClient,
                            Map.of(),
                            null,
                            Map.of(),
                            discoveryUrl,
                            10_000L,
                            mutinyVertx,
                            false)
                    .await()
                    .indefinitely();

            var issuer = metadata.getString("issuer");
            var jwksUri = metadata.getString("jwks_uri");
            if (issuer == null || issuer.isBlank() || jwksUri == null || jwksUri.isBlank()) {
                throw new IllegalStateException("OIDC discovery document is missing issuer or jwks_uri");
            }

            var jwks = new HttpsJwks(jwksUri);
            var consumerBuilder = new JwtConsumerBuilder()
                    .setVerificationKeyResolver(new HttpsJwksVerificationKeyResolver(jwks))
                    .setExpectedIssuer(issuer)
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(30);

            clientId.ifPresent(consumerBuilder::setExpectedAudience);

            return new OidcTokenValidator(
                    vertx,
                    consumerBuilder.build(),
                    trimTrailingSlash(issuerUrl),
                    clientId
            );
        } finally {
            webClient.close();
        }
    }

    Optional<String> validate(String token) {
        try {
            JwtClaims claims = jwtConsumer.processToClaims(token);
            if (!issuerMatches(claims)) {
                return Optional.empty();
            }
            if (!audienceMatches(claims)) {
                return Optional.empty();
            }
            return Optional.of(resolvePrincipalName(claims));
        } catch (InvalidJwtException | MalformedClaimException ex) {
            return Optional.empty();
        }
    }

    private boolean issuerMatches(JwtClaims claims) throws MalformedClaimException {
        var issuer = claims.getIssuer();
        return issuer != null && trimTrailingSlash(issuer).equals(issuerUrl);
    }

    private boolean audienceMatches(JwtClaims claims) throws MalformedClaimException {
        if (clientId.isEmpty()) {
            return true;
        }

        var expectedClientId = clientId.get();
        var audiences = claims.getAudience();
        if (audiences != null && audiences.contains(expectedClientId)) {
            return true;
        }

        var authorizedParty = claims.getStringClaimValue("azp");
        return expectedClientId.equals(authorizedParty);
    }

    private static String resolvePrincipalName(JwtClaims claims) throws MalformedClaimException {
        var preferredUsername = claims.getStringClaimValue("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        var subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("Token does not contain preferred_username or sub claim");
        }
        return subject;
    }

    @Override
    public void close() {
        vertx.close();
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
