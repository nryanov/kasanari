package kasanari.core;

@FunctionalInterface
public interface ThrowableSupplier<T> {
    T get() throws Exception;
}
