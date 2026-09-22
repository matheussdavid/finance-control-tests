package br.com.financecontrol.tests.web;

import br.com.financecontrol.core.WebTestBase;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.fixtures.TestUserFixture;
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
    @DisplayName("Deve exibir o dashboard após login válido")
    void shouldLoginSuccessfully() {
        TestUser user = userFixture.registerUniqueUser();

        loginPage.open().loginAs(user.username(), user.password());

        assertThat(dashboardPage.isDisplayed()).isTrue();
    }
}