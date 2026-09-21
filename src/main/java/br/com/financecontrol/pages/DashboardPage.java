package br.com.financecontrol.pages;

import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Dashboard (rota {@code /}): visível quando o usuário está autenticado. */
public class DashboardPage extends BasePage {

    private static final By PAGE = By.cssSelector("[data-testid='dashboard-page']");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /** Navega diretamente para o dashboard (sem passar por outra página). */
    public DashboardPage open() {
        navigate(ConfigManager.BASE_URL + "/");
        return this;
    }

    /** {@code true} quando o dashboard carregou (sinal de login bem-sucedido). */
    public boolean isDisplayed() {
        return isVisible(PAGE);
    }
}