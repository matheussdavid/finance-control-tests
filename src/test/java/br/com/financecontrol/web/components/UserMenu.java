package br.com.financecontrol.web.components;

import br.com.financecontrol.web.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Menu do usuário na topnav (shared no {@code Layout}: presente em todas as
 * rotas autenticadas). Gerencia o dropdown e o logout client-side.
 */
public class UserMenu extends BasePage {

    private static final By USER_MENU_BTN = By.cssSelector("[data-testid='layout-topnav-user-menu-btn']");
    private static final By LOGOUT_BTN = By.cssSelector("[data-testid='layout-topnav-user-logout-btn']");

    /** Chave do token de sessão usada pelo app (frontend/src/services/api.ts). */
    private static final String TOKEN_KEY = "finance_control_token";

    public UserMenu(WebDriver driver) {
        super(driver);
    }

    /** Abre o dropdown do menu do usuário. */
    public UserMenu open() {
        click(USER_MENU_BTN);
        return this;
    }

    /** Faz logout: abre o dropdown se necessário e clica em logout. */
    public UserMenu logout() {
        if (!present(LOGOUT_BTN)) {
            open();
        }
        click(LOGOUT_BTN);
        return this;
    }

    /** Token de sessão atual no {@code localStorage}, ou {@code null} após logout. */
    public String storedToken() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js.executeScript("return localStorage.getItem(arguments[0]);", TOKEN_KEY);
    }
}