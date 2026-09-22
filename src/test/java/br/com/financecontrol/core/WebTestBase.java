package br.com.financecontrol.core;

import br.com.financecontrol.driver.DriverFactory;
import br.com.financecontrol.web.pages.DashboardPage;
import br.com.financecontrol.web.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;

/**
 * Classe base dos testes web: cria um WebDriver por teste e expõe as páginas.
 * O encerramento do driver e a captura de evidências ficam no
 * {@link WebTestWatcher} (suas callbacks rodam depois de {@code @AfterEach}).
 */
public abstract class WebTestBase extends TestBase {

    @RegisterExtension
    static final WebTestWatcher WATCHER = new WebTestWatcher();

    protected WebDriver driver;
    protected LoginPage loginPage;
    protected DashboardPage dashboardPage;

    @BeforeEach
    void initDriver() {
        driver = DriverFactory.create();
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
    }
}