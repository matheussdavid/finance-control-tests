package br.com.financecontrol.tests.web;

import br.com.financecontrol.core.WebTestBase;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.fixtures.TestUserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EXEMPLO 2 — teste web.
 *
 * <p>Observe: o teste não conhece locators, WebDriver, waits ou URLs.
 * Estado é criado via API (fixture) e as interações são compostas pelos
 * Page Objects (LoginPage/DashboardPage).
 */
@Tag("web")
class LoginWebTest extends WebTestBase {

    private final TestUserFixture userFixture = new TestUserFixture();

    @Test
    @Tag("smoke")
    @DisplayName("Deve exibir o dashboard após login válido")
    void shouldLoginSuccessfully() {
        TestUser user = userFixture.registerUniqueUser();

        loginPage.open().loginAs(user.username(), user.password());

        assertThat(dashboardPage.isDisplayed()).isTrue();
    }

    @Test
    @Tag("smoke")
    @DisplayName("Deve exibir erro ao informar senha incorreta")
    void shouldShowErrorForInvalidPassword() {
        TestUser user = userFixture.registerUniqueUser();

        loginPage.open().loginAs(user.username(), "senha-errada");

        assertThat(loginPage.isOnLoginPage()).isTrue();
        assertThat(loginPage.getErrorMessage()).contains("Usuário ou senha inválidos");
    }

    @Test
    @DisplayName("Deve redirecionar para o login quando não autenticado")
    void shouldRedirectToLoginWhenNotAuthenticated() {
        dashboardPage.open();

        assertThat(loginPage.isDisplayed()).isTrue();
    }
}