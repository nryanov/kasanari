package kasanari.catalog.iceberg.kasanari.repository;

public interface CatalogRepository {
    void register();

    boolean exists();

    default boolean notExists() {
        return !exists();
    }
}
