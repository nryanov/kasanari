package kasanari.authentication.runtime;

import io.vertx.ext.web.RoutingContext;
import kasanari.authentication.spi.AuthRequest;

import java.util.HashMap;
import java.util.Map;

public final class AuthRequests {
    private AuthRequests() {
    }

    static AuthRequest from(RoutingContext context) {
        var headers = new HashMap<String, String>();
        for (var name : context.request().headers().names()) {
            headers.put(name, context.request().getHeader(name));
        }

        return new AuthRequest(
                context.request().method().name(),
                context.normalizedPath(),
                Map.copyOf(headers),
                context.request().remoteAddress() == null ? null : context.request().remoteAddress().host()
        );
    }
}
