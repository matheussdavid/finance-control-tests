package br.com.financecontrol.tests.web;

import br.com.financecontrol.api.models.UserInfo;
import br.com.financecontrol.core.WebTestBase;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.fixtures.TestUserFixture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes web de login. Estado é criado via API (fixture) e as interações são
 * compostas pelos Page Objects (LoginPage/DashboardPage).
 */
@Tag("web")
class LoginWebTest extends WebTestBase {

    private final TestUserFixture userFixture = new TestUserFixture();

    @Test
    @Tag("smoke")
    @DisplayName("Login com usuario válido exibe o dashboard")
    void deveAutenticarUsuarioComCredenciaisValidas() {
        TestUser user = userFixture.registerUniqueUser();

        loginPage.open().loginAs(user.username(), user.password());

        assertThat(dashboardPage.isDisplayed()).isTrue();
    }

    @Test
    @Tag("smoke")
    @DisplayName("Login com email válido exibe o dashboard")
    void deveAutenticarUsuarioComEmailValido() {
        TestUser user = userFixture.registerUniqueUser();

        loginPage.open().loginAs(user.email(), user.password());

        assertThat(dashboardPage.isDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Identificador em branco exibe validação e não chama a API")
    void deveExibirMensagemQuandoIdentificadorEmBranco() {
        loginPage.open().loginAs("", "123456789");

        assertThat(loginPage.getErrorMessage()).isEqualTo(
                "Informe seu usuário ou e-mail");
        assertThat(loginPage.isDisplayed()).isTrue();
    }

    @Test
    @DisplayName("Senha menor que 8 caracteres exibe validação")
    void deveExibirMensagemQuandoSenhaMenorQue8Caracteres() {
        loginPage.open().loginAs("tester", "1234567");

        assertThat(loginPage.getErrorMessage()).isEqualTo("A senha deve ter no mínimo 8 caracteres");
    }

    @Test
    @DisplayName("Credenciais inválidas exibem erro da API e permanecem na página")
    void deveExibirMensagemQuandoUsuarioInformaCredenciaisInvalidas() {
        loginPage.open().loginAs("tester", "123456789");

        assertThat(loginPage.getErrorMessage()).isEqualTo("Usuário ou senha inválidos");
    }

    @Test
    @DisplayName("Alternar entre login e cadastro navega e limpa o erro")
    void deveLimparErroAoTrocarEntreLoginECadastro() {
        loginPage.open().loginAs("", "123456789");

        assertThat(loginPage.hasErrorMessage()).isTrue();

        loginPage.goToRegister();

        assertThat(registerPage.isDisplayed()).isTrue();
        assertThat(registerPage.hasErrorMessage()).isFalse();

        registerPage.goToLogin();

        assertThat(loginPage.isDisplayed()).isTrue();
        assertThat(loginPage.hasErrorMessage()).isFalse();
    }

    @Test
    @DisplayName("Usuario autenticado que acessa /login e redirecionado para o dashboard")
    void deveRedirecionarUsuarioAutenticadoAcessandoLoginParaDashboard() throws JsonProcessingException {
        TestUser user = userFixture.registerUniqueUser();

        String userJson = new ObjectMapper().writeValueAsString(
                new UserInfo(user.userId(), user.name(), user.email()));

        loginPage.seedStoredSession(user.token(), userJson)
                .openWhenAlreadyAuthenticated();

        assertThat(dashboardPage.isDisplayed()).isTrue();
        assertThat(driver.getCurrentUrl()).doesNotContain("/login");
    }

    @Test
    @DisplayName("Logout na navbar limpa a sessao e redireciona para /login")
    void deveLimparSessaoERedirecionarParaLoginAoFazerLogout() throws JsonProcessingException {
        TestUser user = userFixture.registerUniqueUser();
        String userJson = new ObjectMapper().writeValueAsString(
                new UserInfo(user.userId(), user.name(), user.email()));

        loginPage.seedStoredSession(user.token(), userJson)
                .openWhenAlreadyAuthenticated();
        assertThat(dashboardPage.isDisplayed()).isTrue();

        userMenu.logout();

        assertThat(loginPage.isDisplayed()).isTrue();
        assertThat(userMenu.storedToken()).isNull();

        dashboardPage.open();

        assertThat(loginPage.isDisplayed()).isTrue();
    }
}