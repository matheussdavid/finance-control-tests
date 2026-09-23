package br.com.financecontrol.tests.web;

import br.com.financecontrol.api.requests.RegisterRequest;
import br.com.financecontrol.builders.TestUserBuilder;
import br.com.financecontrol.core.WebTestBase;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.fixtures.TestUserFixture;
import br.com.financecontrol.web.pages.RegisterPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("web")
public class RegisterWebTest extends WebTestBase {

    private final TestUserFixture userFixture = new TestUserFixture();

    @Test
    @Tag("smoke")
    @DisplayName("Cadastro com dados válidos exibe o dashboard")
    void deveRegistrarUsuarioComSucessoERedirecionarParaDashboard() {
        TestUser user = TestUserBuilder.aUser().build();

        registerPage.open().register(user.toRegisterRequest());

        assertThat(dashboardPage.isDisplayed()).isTrue();
    }

    @ParameterizedTest(name = "Cadastro com {0} em branco exibe \"{1}\"")
    @MethodSource("camposObrigatoriosEmBranco")
    @DisplayName("Cadastro com campo obrigatório em branco exibe a validação correspondente")
    void deveExibirValidacaoQuandoCampoObrigatorioEstiverEmBranco(RegisterPage.RegisterField field, String expectedMsg) {
            registerPage.open()
                    .fillValidData(TestUserBuilder.aUser().build().toRegisterRequest())
                    .clearField(field)
                    .submit();

            assertThat(registerPage.getErrorMessage()).isEqualTo(expectedMsg);
            assertThat(registerPage.isDisplayed()).isTrue();
    }

    static Stream<Arguments> camposObrigatoriosEmBranco() {
        return Stream.of(
                Arguments.of(RegisterPage.RegisterField.NAME,            "Informe seu nome"),
                Arguments.of(RegisterPage.RegisterField.USERNAME,        "Informe um usuário"),
                Arguments.of(RegisterPage.RegisterField.EMAIL,           "Informe seu e-mail"),
                Arguments.of(RegisterPage.RegisterField.PASSWORD,        "Informe sua senha"),
                Arguments.of(RegisterPage.RegisterField.CONFIRM_PASSWORD, "Confirme sua senha")
        );
    }

    @Test
    @DisplayName("Email inválido no cadastro exibe validação")
    void deveExibirValidacaoDeEmailInvalidoQuandoFormatoForIncorreto() {
        TestUser user = TestUserBuilder.aUser().withEmail("email@").build();

        registerPage.open().register(user.toRegisterRequest());

        assertThat(registerPage.getErrorMessage()).isEqualTo("Informe um e-mail válido");
        assertThat(registerPage.isDisplayed()).isTrue();

    }

    @Test
    @DisplayName("Senha e confirmação diferentes exibem validação")
    void deveExibirValidacaoQuandoSenhaEConfirmacaoForemDiferentes() {
        TestUser user = TestUserBuilder.aUser().build();
        RegisterRequest request = new RegisterRequest(
                user.name(),
                user.username(),
                user.email(),
                "12345678",
                "123456789"
        );

        registerPage.open().register(request);

        assertThat(registerPage.getErrorMessage()).isEqualTo("As senhas não coincidem");
        assertThat(registerPage.isDisplayed()).isTrue();

    }

    @ParameterizedTest(name = "Cadastro com {0} já cadastrado exibe \"{1}\"")
    @MethodSource("camposJaCadastrados")
    void deveExibirValidacaoQuandoUsernameOuEmailJaEstiveremCadastrados(String field, String expectedMessage) {
        TestUser user = userFixture.registerUniqueUser();

        TestUser newUser = field.equals("username")
                ? TestUserBuilder.aUser()
                    .withUsername(user.username())
                    .build()
                : TestUserBuilder.aUser()
                    .withEmail(user.email())
                    .build();

        registerPage.open()
                .fillValidData(newUser.toRegisterRequest())
                .submit();

        assertThat(registerPage.getErrorMessage()).isEqualTo(expectedMessage);
        assertThat(registerPage.isDisplayed()).isTrue();
    }

    static Stream<Arguments> camposJaCadastrados() {
        return Stream.of(
                Arguments.of("username", "Usuário já cadastrado"),
                Arguments.of("email", "E-mail já cadastrado")
        );
    }

    @ParameterizedTest(name = "{0} com 1 caractere exibe \"{1}\"")
    @MethodSource("camposComTamanhoInvalido")
    void deveExibirValidacaoQuandoNomeOuUsernameTiverMenosDeDoisCaracteres(String field, String expectedMessage) {
        TestUser user = userFixture.registerUniqueUser();

        TestUser newUser = field.equals("name")
                ? TestUserBuilder.aUser().withName("A").build()
                : TestUserBuilder.aUser().withUsername("A").build();

        registerPage.open()
                .fillValidData(newUser.toRegisterRequest())
                .submit();

        assertThat(registerPage.getErrorMessage()).isEqualTo(expectedMessage);
        assertThat(registerPage.isDisplayed()).isTrue();
    }

    static Stream<Arguments> camposComTamanhoInvalido() {
        return Stream.of(
                Arguments.of("name", "O nome deve ter no mínimo 2 caracteres"),
                Arguments.of("username", "O usuário deve ter no mínimo 2 caracteres")
        );
    }
}
