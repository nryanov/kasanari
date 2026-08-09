package kasanari.repository.jdbc;

import java.util.ServiceLoader;
import java.util.stream.Collectors;

public final class BackendFactoryLoader {
    private BackendFactoryLoader() {
    }

    public static <T extends BackendAwareFactory> T load(Class<T> type, RepositoryBackend backend) {
        var matches = ServiceLoader.load(type).stream()
                .map(ServiceLoader.Provider::get)
                .filter(factory -> factory.backend() == backend)
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            throw new IllegalStateException(
                    "No " + type.getSimpleName() + " registered for backend `" + backend
                            + "`. Ensure the corresponding repository module is on the classpath."
            );
        }
        if (matches.size() > 1) {
            throw new IllegalStateException(
                    "Multiple " + type.getSimpleName() + " implementations registered for backend `"
                            + backend + "`."
            );
        }
        return matches.getFirst();
    }
}
