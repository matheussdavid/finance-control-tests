package br.com.financecontrol.web.pages;

import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Página de login do app (rota {@code /login}).
 * Locators usam os {@code data-testid} fornecidos pela aplicação.
 */
public class LoginPage extends BasePage {

    private static final By PAGE = By.cssSelector("[data-testid='login-page']");
    private static final By IDENTIFIER_INPUT = By.cssSelector("[data-testid='login-identifier-input']");
    private static final By PASSWORD_INPUT = By.cssSelector("[data-testid='login-password-input']");
    private static final By SUBMIT_BTN = By.cssSelector("[data-testid='login-submit-btn']");
    private static final By ERROR_MESSAGE = By.cssSelector("[data-testid='message-error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** Abre a página de login. */
    public LoginPage open() {
        navigate(ConfigManager.BASE_URL + "/login");
        waitVisible(PAGE);
        return this;
    }

    /**
     * Preenche credenciais e submete o formulário.
     * Aguarda a navegação (sucesso) ou a exibição do erro, retornando ao final.
     */
    public LoginPage loginAs(String identifier, String password) {
        fill(IDENTIFIER_INPUT, identifier);
        fill(PASSWORD_INPUT, password);
        click(SUBMIT_BTN);
        waitForLoginResult();
        return this;
    }

    /** {@code true} quando o formulário de login está visível. */
    public boolean isDisplayed() {
        return isVisible(PAGE);
    }

    private void waitForLoginResult() {
        try {
            WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.WAIT_TIMEOUT_SECONDS));
            // present() faz checagem rápida (2s); um wait completo aqui travaria o
            // polling da URL enquanto a aplicação já navegou para o dashboard.
            loginWait.until(d -> !d.getCurrentUrl().contains("/login") || present(ERROR_MESSAGE));
        } catch (TimeoutException e) {
            throw new TimeoutException("Tempo esgotado aguardando resultado do login", e);
        }
    }

    public String getErrorMessage() {
        return waitVisible(ERROR_MESSAGE).getText();
    }
}