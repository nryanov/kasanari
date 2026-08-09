package kasanari.repository.jdbc;

public interface BackendAwareFactory {
    RepositoryBackend backend();
}
