package kasanari.catalog.iceberg.repository;

public interface CatalogRepository<T> {
    void register(T tx);

    boolean exists(T tx);

    default boolean notExists(T tx) {
        return !exists(tx);
    }
}
