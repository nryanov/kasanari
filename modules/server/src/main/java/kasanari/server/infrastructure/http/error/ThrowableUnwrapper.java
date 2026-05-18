package kasanari.server.infrastructure.http.error;

public final class ThrowableUnwrapper {
    private ThrowableUnwrapper() {
    }

    public static Throwable unwrap(Throwable throwable) {
        var root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }
}
