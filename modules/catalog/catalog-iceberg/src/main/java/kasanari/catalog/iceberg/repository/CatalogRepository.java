package kasanari.catalog.iceberg.repository;

public interface CatalogRepository {
    void register();

    boolean exists();

    default boolean notExists() {
        return !exists();
    }
}
