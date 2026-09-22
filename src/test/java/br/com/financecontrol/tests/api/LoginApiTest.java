package br.com.financecontrol.tests.api;

import br.com.financecontrol.api.clients.AuthClient;
import br.com.financecontrol.api.models.AuthResponse;
import br.com.financecontrol.api.requests.LoginRequest;
import br.com.financecontrol.core.TestBase;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.fixtures.TestUserFixture;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes da API de autenticação. O teste de contrato (JSON Schema) do mesmo
 * endpoint fica nesta classe, marcado com {@code @Tag("contract")}.
 */
@Tag("api")
class LoginApiTest extends TestBase {

    private final AuthClient authClient = new AuthClient();
    private final TestUserFixture userFixture = new TestUserFixture();

    @Test
    @Tag("contract")
    @Tag("smoke")
    @DisplayName("Resposta de login válido respeita o contrato")
    void shouldMatchLoginResponseSchema() {
        TestUser user = userFixture.registerUniqueUser();

        var response = authClient.login(LoginRequest.of(user.username(), user.password()));

        response.then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth/login-response.json"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("Deve autenticar usuário usando o username")
    void shouldLoginWithValidUsername() {
        TestUser user = userFixture.registerUniqueUser();

        Response response = authClient.login(LoginRequest.of(user.username(), user.password()));

        assertThat(response.statusCode()).isEqualTo(200);
        AuthResponse auth = response.then().extract().as(AuthResponse.class);
        assertThat(auth.token()).isNotBlank();
        assertThat(auth.user().id()).isEqualTo(user.userId());
    }
}