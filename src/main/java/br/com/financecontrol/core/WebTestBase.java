package br.com.financecontrol.core;

import br.com.financecontrol.driver.DriverManager;
import br.com.financecontrol.pages.DashboardPage;
import br.com.financecontrol.pages.LoginPage;
import br.com.financecontrol.pages.components.HeaderComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Classe base para testes web: cria o WebDriver por teste e anexa o watcher de
 * evidências. O encerramento do driver é de responsabilidade do
 * {@link WebTestWatcher} (roda depois do {@code @AfterEach}).
 */
public abstract class WebTestBase {

    @RegisterExtension
    static final WebTestWatcher WATCHER = new WebTestWatcher();

    protected LoginPage loginPage;
    protected DashboardPage dashboardPage;
    protected HeaderComponent header;

    @BeforeAll
    static void webBaseSetup() {
        TestSetup.init();
    }

    @BeforeEach
    void webBaseBeforeEach() {
        var driver = DriverManager.open();
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
        header = new HeaderComponent(driver);
    }
}