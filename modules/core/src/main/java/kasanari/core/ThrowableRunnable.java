package kasanari.core;

@FunctionalInterface
public interface ThrowableRunnable {
    void run() throws Exception;
}
