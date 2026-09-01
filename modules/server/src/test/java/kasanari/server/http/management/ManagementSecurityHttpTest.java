package kasanari.server.http.management;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.RoleBinding;
import kasanari.authorization.spi.RoleBindingAdministration;
import kasanari.server.http.CatalogHttpTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ManagementSecurityHttpTest extends CatalogHttpTestSupport {
    @InjectMock
    AuthorizationService authorizationService;

    private RoleBindingAdministration roleBindings;

    @BeforeEach
    void stubAuthorization() {
        roleBindings = mock(RoleBindingAdministration.class);
        when(authorizationService.denyUnless(any(), anyString(), any())).thenReturn(Optional.empty());
        when(authorizationService.roleBindingsOrThrow()).thenReturn(roleBindings);
        when(authorizationService.subject(any())).thenReturn("anonymous");
    }

    @Test
    void getRolesRequiresResourceQueryParam() {
        givenJson()
                .when()
                .get("/management/v1/security/roles")
                .then()
                .statusCode(400)
                .body("message", equalTo("Resource is required"));
    }

    @Test
    void getRolesParsesSubjectAndResource() {
        when(roleBindings.list("demo", "iceberg/C_0"))
                .thenReturn(List.of(new RoleBinding("demo", "iceberg/C_0", "IcebergCatalogViewer", "allow")));

        givenJson()
                .queryParam("subject", "demo")
                .queryParam("resource", "iceberg/C_0")
                .when()
                .get("/management/v1/security/roles")
                .then()
                .statusCode(200)
                .body("bindings", hasSize(1))
                .body("bindings[0].subject", equalTo("demo"))
                .body("bindings[0].role", equalTo("IcebergCatalogViewer"))
                .body("bindings[0].effect", equalTo("allow"));

        verify(roleBindings).list("demo", "iceberg/C_0");
    }

    @Test
    void addRolesParsesBindingsAndNormalizesEffect() {
        givenJson()
                .body("""
                        {
                          "bindings": [
                            {
                              "subject": "demo",
                              "role": "IcebergCatalogAdmin",
                              "resource": "iceberg/C_0",
                              "effect": "ALLOW"
                            }
                          ]
                        }
                        """)
                .when()
                .post("/management/v1/security/roles")
                .then()
                .statusCode(204);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleBinding>> captor = ArgumentCaptor.forClass(List.class);
        verify(roleBindings).add(captor.capture());
        var binding = captor.getValue().getFirst();
        assertEquals("demo", binding.subject());
        assertEquals("IcebergCatalogAdmin", binding.role());
        assertEquals("iceberg/C_0", binding.resource());
        assertEquals("allow", binding.effect());
    }

    @Test
    void deleteRolesParsesBindings() {
        givenJson()
                .body("""
                        {
                          "bindings": [
                            {
                              "subject": "demo",
                              "role": "IcebergCatalogViewer",
                              "resource": "iceberg/C_0",
                              "effect": "deny"
                            }
                          ]
                        }
                        """)
                .when()
                .delete("/management/v1/security/roles")
                .then()
                .statusCode(204);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleBinding>> captor = ArgumentCaptor.forClass(List.class);
        verify(roleBindings).delete(captor.capture());
        assertEquals("deny", captor.getValue().getFirst().effect());
        assertEquals("IcebergCatalogViewer", captor.getValue().getFirst().role());
    }
}
