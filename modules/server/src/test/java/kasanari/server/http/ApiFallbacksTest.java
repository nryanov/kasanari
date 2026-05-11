package kasanari.server.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ApiFallbacksTest {
    @Test
    void shouldReturnDeterministicNotImplementedPayload() {
        var response = ApiFallbacks.notImplemented("AnyService.anyMethod");
        Assertions.assertEquals(501, response.getStatus());

        @SuppressWarnings("unchecked")
        var payload = (Map<String, String>) response.getEntity();
        Assertions.assertEquals("AnyService.anyMethod", payload.get("handler"));
        Assertions.assertTrue(payload.get("whyItMatters").contains("deterministic"));
    }
}
