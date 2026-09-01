package kasanari.server.http.paimon;

import io.quarkus.test.junit.QuarkusTest;
import kasanari.server.http.CatalogHttpTestSupport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class PaimonConfigHttpTest extends CatalogHttpTestSupport {
    @Test
    void getConfigParsesWarehouseQueryParam() {
        givenJson()
                .queryParam("warehouse", CATALOG)
                .when()
                .get("/paimon/v1/config")
                .then()
                .statusCode(200)
                .body("overrides.prefix", equalTo(CATALOG));
    }
}
