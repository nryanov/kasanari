package kasanari.authentication.oidc.internal;

import io.quarkus.oidc.common.runtime.config.OidcCommonConfig;

import java.nio.file.Path;
import java.util.Optional;

record SimpleOidcTls(Optional<String> tlsConfigurationName, Optional<Verification> verification,
                     Optional<Path> keyStoreFile, Optional<String> keyStoreFileType, Optional<String> keyStoreProvider,
                     Optional<String> keyStorePassword, Optional<String> keyStoreKeyAlias,
                     Optional<String> keyStoreKeyPassword, Optional<Path> trustStoreFile,
                     Optional<String> trustStorePassword, Optional<String> trustStoreCertAlias,
                     Optional<String> trustStoreFileType,
                     Optional<String> trustStoreProvider) implements OidcCommonConfig.Tls {

    static SimpleOidcTls empty() {
        return new SimpleOidcTls(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }
}
