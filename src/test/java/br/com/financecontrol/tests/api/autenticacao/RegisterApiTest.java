package br.com.financecontrol.tests.api.autenticacao;

import br.com.financecontrol.api.clients.AuthClient;
import br.com.financecontrol.api.models.AuthResponse;
import br.com.financecontrol.api.requests.RegisterRequest;
import br.com.financecontrol.builders.TestUserBuilder;
import br.com.financecontrol.core.TestBase;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.data.UserFaker;
import br.com.financecontrol.fixtures.TestUserFixture;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@Tag("api")
class RegisterApiTest extends TestBase {

    private final AuthClient authClient = new AuthClient();
    private final TestUserFixture userFixture = new TestUserFixture();

    @Test
    @Tag("contract")
    @Tag("smoke")
    @DisplayName("Contrato do response de sucesso do POST /auth/register deve retornar 200")
    void deveRetornar200QuandoRegisterForValidoEValidarContrato() {
        TestUser user = TestUserBuilder.aUser().build();

        Response response = authClient.register(user.toRegisterRequest());

        response.then()
                .assertThat()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/auth/register-response.json"));
    }

    @Test
    @Tag("contract")
    @DisplayName("Contrato do response de erro do POST /auth/register deve retornar 409")
    void deveValidarContratoDeErroDoRegister() {
        TestUser user = TestUserBuilder.aUser().build();
        authClient.register(user.toRegisterRequest());

        Response response = authClient.register(user.toRegisterRequest());

        response.then()
                .assertThat()
                .statusCode(409)
                .body("error", equalTo("BUSINESS_RULE_VIOLATION"))
                .body("message", equalTo("E-mail já cadastrado"))
                .body("fields", nullValue())
                .body(matchesJsonSchemaInClasspath("schemas/auth/error-response.json"));
    }

    @Test
    @Tag("smoke")
    @DisplayName("Cadastro com dados válidos cria usuário e deve retornar 200 com token e usuário")
    void deveRetornar200QuandoCadastraUmUsuarioComDadosValidos() {
        TestUser user = TestUserBuilder.aUser().build();

        Response response = authClient.register(user.toRegisterRequest());

        assertThat(response.statusCode()).isEqualTo(201);
        AuthResponse auth = response.then().extract().as(AuthResponse.class);
        assertThat(auth.token()).isNotBlank();
        assertThat(auth.user().id()).isNotBlank();
        assertThat(auth.user().name()).isEqualTo(user.name());
        assertThat(auth.user().email()).isEqualTo(user.email());
    }

    @Test
    @DisplayName("Cadastro com email já cadastrado retorna 409")
    void deveRetornar409AoTentarCadastrarUmUsuarioComEmailJaCadastrado() {
        TestUser user = userFixture.registerUniqueUser();
        TestUser newUser = TestUserBuilder.aUser().withEmail(user.email()).build();

        Response response = authClient.register(newUser.toRegisterRequest());

        response.then()
                .assertThat()
                .statusCode(409)
                .body("error", equalTo("BUSINESS_RULE_VIOLATION"))
                .body("message", equalTo("E-mail já cadastrado"));
    }

    @Test
    @DisplayName("Cadastro com username já cadastrado retorna 409")
    void deveRetornar409AoTentarCadastrarUmUsuarioComUsernameEmailJaCadastrado() {
        TestUser user = userFixture.registerUniqueUser();
        TestUser newUser = TestUserBuilder.aUser().withUsername(user.username()).build();

        Response response = authClient.register(newUser.toRegisterRequest());

        response.then()
                .assertThat()
                .statusCode(409)
                .body("error", equalTo("BUSINESS_RULE_VIOLATION"))
                .body("message", equalTo("Usuário já cadastrado"));
    }

    @Test
    @DisplayName("Cadastro com senha e confirmação diferentes retorna 409")
    void deveRetornar409AoTentarCadastrarUmUsuarioComSenhaDiferenteDaConfirmacao() {
        TestUser user = TestUserBuilder.aUser().build();
        RegisterRequest request = new RegisterRequest(
                user.name(),
                user.username(),
                user.email(),
                "12345678",
                "123456789"
        );

        Response response = authClient.register(request);
        response.then()
                .assertThat()
                .statusCode(409)
                .body("error", equalTo("BUSINESS_RULE_VIOLATION"))
                .body("message", equalTo("As senhas não coincidem"));
    }

    @Test
    @DisplayName("Campos obrigatórios ausentes retornam 400 VALIDATION_ERROR com fields")
    void deveRetornar400AoTentarCadastrarUsuarioSemInformarOsCamposObrigatorios() {
        Response response = authClient.register("{}");

        response.then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_ERROR"))
                .body("message", equalTo("Falha na validação"))
                .body("fields.name", equalTo("O nome é obrigatório"))
                .body("fields.username", equalTo("O usuário é obrigatório"))
                .body("fields.email", equalTo("O e-mail é obrigatório"))
                .body("fields.password", equalTo("A senha é obrigatória"))
                .body("fields.confirmPassword", equalTo("A confirmação de senha é obrigatória"));
    }

    @Test
    @DisplayName("Cadastro com email malformado retorna 400 VALIDATION_ERROR")
    void deveRetornar400AoTentarCadastrarUmUsuarioComEmailInvalido() {
        TestUser user = TestUserBuilder.aUser().withEmail("email.com.br").build();
        Response response = authClient.register(user.toRegisterRequest());

        response.then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_ERROR"))
                .body("message", equalTo("Falha na validação"))
                .body("fields.email", equalTo("O e-mail deve ser válido"));
    }

    @Test
    @DisplayName("Cadastro com senha abaixo do mínimo retorna 400 VALIDATION_ERROR")
    void deveRetornar400AoTentarCadastrarUmUsuarioComSenhaMenorAbaixoDoMinimo() {
        TestUser user = TestUserBuilder.aUser().withPassword("12345").build();
        Response response = authClient.register(user.toRegisterRequest());

        response.then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_ERROR"))
                .body("message", equalTo("Falha na validação"))
                .body("fields.password", equalTo("A senha deve ter entre 8 e 30 caracteres"));
    }

    @Test
    @DisplayName("Cadastro com senha só de espaços retorna 400 VALIDATION_ERROR")
    void deveRetornar400AoTentarCadastrarUmUsuarioComSenhaEConfirmacaoSendoEspacos() {
        TestUser user = TestUserBuilder.aUser().withPassword("        ").build();
        Response response = authClient.register(user.toRegisterRequest());

        response.then()
                .assertThat()
                .statusCode(400)
                .body("error", equalTo("VALIDATION_ERROR"))
                .body("message", equalTo("Falha na validação"))
                .body("fields.password", equalTo("A senha é obrigatória"))
                .body("fields.confirmPassword", equalTo("A confirmação de senha é obrigatória"));
    }


    // --- CT-017: Limites da Senha (min=8, max=30) ---

    @ParameterizedTest(name = "Senha com {0} caracteres deve retornar HTTP {1}")
    @MethodSource("passwordBoundaryProvider")
    @DisplayName("Validação de Bordas (BVA) - Limites da senha")
    void deveValidarLimitesDaSenha(int length, int expectedStatus) {

        String password = UserFaker.stringOfLength(length);
        TestUser user = TestUserBuilder.aUser().withPassword(password).build();

        Response response = authClient.register(user.toRegisterRequest());

        if (expectedStatus == 400) {
            response.then()
                    .assertThat()
                    .statusCode(400)
                    .body("error", equalTo("VALIDATION_ERROR"))
                    .body("fields.password", equalTo("A senha deve ter entre 8 e 30 caracteres"))
                    .body("fields.confirmPassword", equalTo("A senha deve ter entre 8 e 30 caracteres"));
        } else {
            assertThat(response.statusCode())
                    .as("Registro deveria ter sucesso com senha de %d caracteres", length)
                    .isEqualTo(expectedStatus);
        }
    }

    static Stream<Arguments> passwordBoundaryProvider() {
        return Stream.of(
                Arguments.of(7, 400),   // Min - 1 (Inválido)
                Arguments.of(8, 201),   // Min (Válido)
                Arguments.of(30, 201),  // Max (Válido)
                Arguments.of(31, 400)   // Max + 1 (Inválido)
        );
    }

    // --- CT-018: Limites do username (min=2, max=50) ---

    @ParameterizedTest(name = "Username com {0} caracteres deve retornar HTTP {1}")
    @MethodSource("usernameBoundaryProvider")
    @DisplayName("Validação de Bordas (BVA) - Limites do campo username")
    void deveValidarLimitesDoCampoUsername(int length, int expectedStatus) {

        String username = UserFaker.stringOfLength(length);
        TestUser user = TestUserBuilder.aUser().withUsername(username).build();

        Response response = authClient.register(user.toRegisterRequest());

        if (expectedStatus == 400) {
            response.then()
                    .assertThat()
                    .statusCode(400)
                    .body("error", equalTo("VALIDATION_ERROR"))
                    .body("fields.username", equalTo("O usuário deve ter entre 2 e 50 caracteres"));
        } else {
            assertThat(response.statusCode())
                    .as("Registro deveria ter sucesso com senha de %d caracteres", length)
                    .isEqualTo(expectedStatus);
        }
    }

    static Stream<Arguments> usernameBoundaryProvider() {
        return Stream.of(
                Arguments.of(1, 400),   // Min - 1 (Inválido)
                Arguments.of(2, 201),   // Min (Válido)
                Arguments.of(50, 201),  // Max (Válido)
                Arguments.of(51, 400)   // Max + 1 (Inválido)
        );
    }

    // --- CT-018: Limites do name (min=2, max=50) ---

    @ParameterizedTest(name = "Nome com {0} caracteres deve retornar HTTP {1}")
    @MethodSource("nameBoundaryProvider")
    @DisplayName("Validação de Bordas (BVA) - Limites do campo name")
    void deveValidarLimitesDoCampoNome(int length, int expectedStatus) {

        String name = UserFaker.stringOfLength(length);
        TestUser user = TestUserBuilder.aUser().withName(name).build();

        Response response = authClient.register(user.toRegisterRequest());

        if (expectedStatus == 400) {
            response.then()
                    .assertThat()
                    .statusCode(400)
                    .body("error", equalTo("VALIDATION_ERROR"))
                    .body("fields.name", equalTo("O nome deve ter entre 2 e 50 caracteres"));
        } else {
            assertThat(response.statusCode())
                    .as("Registro deveria ter sucesso com senha de %d caracteres", length)
                    .isEqualTo(expectedStatus);
        }
    }

    static Stream<Arguments> nameBoundaryProvider() {
        return Stream.of(
                Arguments.of(1, 400),   // Min - 1 (Inválido)
                Arguments.of(2, 201),   // Min (Válido)
                Arguments.of(50, 201),  // Max (Válido)
                Arguments.of(51, 400)   // Max + 1 (Inválido)
        );
    }

    // --- CT-019: Limites do email (max=50) ---

    @ParameterizedTest(name = "Email com {0} caracteres deve retornar HTTP {1}")
    @MethodSource("emailBoundaryProvider")
    @DisplayName("Validação de Bordas (BVA) - Limites do campo email")
    void deveValidarLimitesDoCampoEmail(int length, int expectedStatus) {

        String email = UserFaker.emailOfLength(length);
        TestUser user = TestUserBuilder.aUser().withEmail(email).build();

        Response response = authClient.register(user.toRegisterRequest());

        if (expectedStatus == 400) {
            response.then()
                    .assertThat()
                    .statusCode(400)
                    .body("error", equalTo("VALIDATION_ERROR"))
                    .body("fields.email", equalTo("O e-mail deve ter no máximo 50 caracteres"));
        } else {
            assertThat(response.statusCode())
                    .as("Registro deveria ter sucesso com senha de %d caracteres", length)
                    .isEqualTo(expectedStatus);
        }
    }

    static Stream<Arguments> emailBoundaryProvider() {
        return Stream.of(
                Arguments.of(49, 201),   // Max - 1 (válido)
                Arguments.of(50, 201),   // Max (Válido)
                Arguments.of(51, 400)   // Max + 1 (Inválido)
        );
    }
}
