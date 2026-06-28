package kasanari.authorization.spi;

public class AuthorizationResource {
    private AuthorizationResource() {}

    public static String build(String... segments) {
        return String.join("/", segments);
    }
}
