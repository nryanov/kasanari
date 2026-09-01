package kasanari.server.http;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import kasanari.catalog.iceberg.IcebergCatalogAdapter;
import kasanari.catalog.lance.LanceCatalogAdapter;
import kasanari.catalog.paimon.PaimonCatalogAdapter;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.server.infrastructure.iceberg.IcebergCatalogRouter;
import kasanari.server.infrastructure.lance.LanceCatalogRouter;
import kasanari.server.infrastructure.paimon.PaimonCatalogRouter;
import org.apache.iceberg.rest.RESTSerializers;
import org.apache.paimon.rest.RESTApi;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Shared harness for {@code @QuarkusTest} HTTP parse tests.
 *
 * <p>Catalog routers and {@link ManagementCatalogService} are mocked so Quarkus never opens the
 * management JDBC pool. Adapters are Mockito mocks returned by {@code getOrThrow}.
 */
public abstract class CatalogHttpTestSupport {
    public static final String CATALOG = "C_0";
    public static final String NAMESPACE = "NS_0";
    public static final String MULTIPART_NAMESPACE = "NS_0\u001FNS_1";
    public static final String TABLE = "T_0";
    public static final String VIEW = "V_0";
    public static final String DATABASE = "DB_0";
    public static final String FUNCTION = "FN_0";
    public static final String BRANCH = "BR_0";
    public static final String TAG = "TAG_0";
    public static final String LANCE_NAMESPACE_ID = "C_0.NS_0";
    public static final String LANCE_TABLE_ID = "C_0.NS_0.T_0";
    public static final String LANCE_CUSTOM_DELIMITER = "$";
    public static final String LANCE_NAMESPACE_ID_CUSTOM = "C_0$NS_0";
    public static final String LANCE_TABLE_ID_CUSTOM = "C_0$NS_0$T_0";

    private static final ObjectMapper ICEBERG_MAPPER = icebergMapper();

    @Inject
    protected ManagementCatalogService managementCatalogService;

    @Inject
    protected IcebergCatalogRouter icebergCatalogRouter;

    @Inject
    protected PaimonCatalogRouter paimonCatalogRouter;

    @Inject
    protected LanceCatalogRouter lanceCatalogRouter;

    protected IcebergCatalogAdapter icebergAdapter;
    protected PaimonCatalogAdapter paimonAdapter;
    protected LanceCatalogAdapter lanceAdapter;

    @BeforeEach
    void stubCatalogRouters() {
        Mockito.reset(managementCatalogService, icebergCatalogRouter, paimonCatalogRouter, lanceCatalogRouter);
        icebergAdapter = mock(IcebergCatalogAdapter.class);
        paimonAdapter = mock(PaimonCatalogAdapter.class);
        lanceAdapter = mock(LanceCatalogAdapter.class);
        lenient().when(icebergCatalogRouter.getOrThrow(eq(CATALOG))).thenReturn(icebergAdapter);
        lenient().when(paimonCatalogRouter.getOrThrow(eq(CATALOG))).thenReturn(paimonAdapter);
        lenient().when(lanceCatalogRouter.getOrThrow(eq(CATALOG))).thenReturn(lanceAdapter);
    }

    protected static RequestSpecification givenJson() {
        return given().contentType(ContentType.JSON).accept(ContentType.JSON);
    }

    protected static String icebergJson(Object value) {
        try {
            return ICEBERG_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Iceberg REST payload", e);
        }
    }

    protected static String paimonJson(Object value) {
        try {
            return RESTApi.toJson(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Paimon REST payload", e);
        }
    }

    private static ObjectMapper icebergMapper() {
        var mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        RESTSerializers.registerAll(mapper);
        return mapper;
    }
}
