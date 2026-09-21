package br.com.financecontrol.pages;

import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

/**
 * Página de login/registro do app (roteiro {@code /login} e {@code /register}).
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

    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/login");
    }

    /** Texto do erro exibido após login malsucedido (vazio se não houver erro). */
    public String getErrorMessage() {
        return present(ERROR_MESSAGE) ? textOf(ERROR_MESSAGE) : "";
    }

    private void waitForLoginResult() {
        long deadline = System.currentTimeMillis() + ConfigManager.WAIT_TIMEOUT_SECONDS * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (!driver.getCurrentUrl().contains("/login") || present(ERROR_MESSAGE)) {
                return;
            }
            sleepQuietly(250);
        }
        throw new TimeoutException("Tempo esgotado aguardando resultado do login");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}