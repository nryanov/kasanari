package kasanari.repository.iceberg;

public interface CatalogRepository<T> {
    void register(T tx);

    boolean exists(T tx);

    default boolean notExists(T tx) {
        return !exists(tx);
    }
}
