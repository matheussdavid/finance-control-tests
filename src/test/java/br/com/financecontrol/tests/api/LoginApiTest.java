package br.com.financecontrol.tests.api;

import br.com.financecontrol.api.clients.AuthClient;
import br.com.financecontrol.api.models.ApiError;
import br.com.financecontrol.api.models.AuthResponse;
import br.com.financecontrol.api.requests.LoginRequest;
import br.com.financecontrol.builders.TestUserBuilder;
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
 * EXEMPLO 1 — teste de API.
 *
 * <p>Observe: o teste usa fixture (massa), client (HTTP), builder e model.
 * Nenhuma chamada HTTP, JSON ou lógica de infraestrutura mora aqui.
 *
 * <p>Os testes de contrato (JSON Schema) do mesmo endpoint ficam nesta classe,
 * marcados com {@code @Tag("contract")} — sem classe separada.
 */
@Tag("api")
class LoginApiTest extends TestBase {

    private final AuthClient authClient = new AuthClient();
    private final TestUserFixture userFixture = new TestUserFixture();

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

    @Test
    @Tag("smoke")
    @DisplayName("Deve autenticar usuário usando o e-mail")
    void shouldLoginWithValidEmail() {
        TestUser user = userFixture.registerUniqueUser();

        Response response = authClient.login(LoginRequest.of(user.email(), user.password()));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.then().extract().as(AuthResponse.class).token()).isNotBlank();
    }

    @Test
    @DisplayName("Deve rejeitar senha incorreta com 401")
    void shouldRejectInvalidPassword() {
        TestUser user = userFixture.registerUniqueUser();

        Response response = authClient.login(LoginRequest.of(user.username(), "senha-errada"));

        assertThat(response.statusCode()).isEqualTo(401);
        ApiError error = authClient.asError(response);
        assertThat(error.status()).isEqualTo(401);
        assertThat(error.error()).isEqualTo("UNAUTHORIZED");
        assertThat(error.message()).isEqualTo("Usuário ou senha inválidos");
    }

    @Test
    @DisplayName("Deve rejeitar usuário inexistente com 401")
    void shouldRejectUnknownUser() {
        Response response = authClient.login(LoginRequest.of("usuario-inexistente", "qualquer-senha"));

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Deve rejeitar login sem senha com erro de validação")
    void shouldRejectMissingPassword() {
        TestUser user = TestUserBuilder.aUser().build();

        Response response = authClient.login(LoginRequest.of(user.username(), ""));

        assertThat(response.statusCode()).isEqualTo(400);
        ApiError error = authClient.asError(response);
        assertThat(error.error()).isEqualTo("VALIDATION_ERROR");
        assertThat(error.fields()).containsKey("password");
    }

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
    @Tag("contract")
    @DisplayName("Erro de autenticação respeita o contrato de erro")
    void shouldMatchErrorResponseSchema() {
        TestUser user = userFixture.registerUniqueUser();

        var response = authClient.login(LoginRequest.of(user.username(), "senha-errada"));

        response.then()
                .assertThat()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/common/error-response.json"));
    }
}