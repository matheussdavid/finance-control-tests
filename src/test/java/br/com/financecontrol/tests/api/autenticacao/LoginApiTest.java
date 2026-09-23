package br.com.financecontrol.tests.api.autenticacao;

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
import static org.hamcrest.Matchers.equalTo;

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
    @DisplayName("Contrato do response de sucesso do POST /auth/login deve retornar 200")
    void deveRetornar200QuandoLoginForValidoEValidarContrato() {
        TestUser user = userFixture.registerUniqueUser();

        var response = authClient.login(LoginRequest.of(user.username(), user.password()));

        response.then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth/login-response.json"));
    }

    @Test
    @Tag("contract")
    @Tag("smoke")
    @DisplayName("Contrato do response de erro do POST /auth/login deve retornar 401")
    void deveRetornar401QuandoLoginForInvalidoEValidarContrato() {
        var response = authClient.login(LoginRequest.of("teste", "teste123456"));

        response.then()
                .assertThat()
                .statusCode(401)
                .body(matchesJsonSchemaInClasspath("schemas/auth/error-response.json"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("Login com username e senha válidos deve retornar 200 com token e usuário")
    void deveRetornar200QuandoUsernameESenhaForemValidos() {
        TestUser user = userFixture.registerUniqueUser();

        Response response = authClient.login(LoginRequest.of(user.username(), user.password()));

        assertThat(response.statusCode()).isEqualTo(200);
        AuthResponse auth = response.then().extract().as(AuthResponse.class);
        assertThat(auth.token()).isNotBlank();
        assertThat(auth.user().id()).isEqualTo(user.userId());
    }

    @Test
    @Tag("smoke")
    @DisplayName("Login com email e senha válidos deve retornar 200 com token e usuário")
    void deveRetornar200QuandoEmailESenhaForemValidos() {
        TestUser user = userFixture.registerUniqueUser();

        Response response = authClient.login(LoginRequest.of(user.email(), user.password()));

        assertThat(response.statusCode()).isEqualTo(200);
        AuthResponse auth = response.then().extract().as(AuthResponse.class);
        assertThat(auth.token()).isNotBlank();
        assertThat(auth.user().id()).isEqualTo(user.userId());
    }

    @Test
    @Tag("smoke")
    @DisplayName("Login com credenciais inválidas deve retornar 401 UNAUTHORIZED")
    void deveRetornar401QuandoCredenciaisForemInvalidas() {
        Response response = authClient.login(LoginRequest.of("user", "senha123456"));

        response.then()
                .assertThat()
                .statusCode(401)
                .body("message", equalTo("Usuário ou senha inválidos"));
    }

    @Test
    @DisplayName("Login com o campo identifier vazio deve retornar 400")
    void deveRetornar400QuandoCampoIdentifierEstiverVazio() {
        Response response = authClient.login(LoginRequest.of("", "senha123456"));

        response.then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Falha na validação"))
                .body("fields.identifier", equalTo("Usuário ou e-mail é obrigatório"));
    }

    @Test
    @DisplayName("Login com o campo password vazio deve retornar 400")
    void deveRetornar400QuandoCampoPasswordEstiverVazio() {
        Response response = authClient.login(LoginRequest.of("user", ""));

        response.then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Falha na validação"))
                .body("fields.password", equalTo("A senha é obrigatória"));
    }

    @Test
    @DisplayName("Login com todos os campos vazios deve retornar 400")
    void deveRetornar400QuandoTodosOsCamposEstiveremVazios() {
        Response response = authClient.login(LoginRequest.of("", ""));

        response.then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Falha na validação"))
                .body("fields.identifier", equalTo("Usuário ou e-mail é obrigatório"))
                .body("fields.password", equalTo("A senha é obrigatória"));
    }

    @Test
    @DisplayName("Login com body JSON vazio deve retornar 400")
    void deveRetornar400QuandoBodyEstiverVazio() {
        Response response = authClient.login("{}");

        response.then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_ERROR"))
                .body("message", equalTo("Falha na validação"))
                .body("fields.identifier", equalTo("Usuário ou e-mail é obrigatório"))
                .body("fields.password", equalTo("A senha é obrigatória"));
    }

    @Test
    @DisplayName("Login com campos explicitamente nulos deve retornar 400")
    void deveRetornar400QuandoCamposForemNulos() {
        Response response = authClient.login(LoginRequest.of(null, null));

        response.then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_ERROR"))
                .body("fields.identifier", equalTo("Usuário ou e-mail é obrigatório"))
                .body("fields.password", equalTo("A senha é obrigatória"));
    }

    @Test
    @DisplayName("Login com campos contendo apenas espaços em branco deve retornar 400")
    void deveRetornar400QuandoCamposContiveremApenasEspacos() {
        Response response = authClient.login(LoginRequest.of("   ", "   "));

        response.then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Falha na validação"))
                .body("fields.identifier", equalTo("Usuário ou e-mail é obrigatório"))
                .body("fields.password", equalTo("A senha é obrigatória"));
    }
}