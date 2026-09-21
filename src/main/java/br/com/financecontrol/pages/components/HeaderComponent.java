package br.com.financecontrol.pages.components;

import br.com.financecontrol.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Barra superior / menu do usuário (comum a todas as páginas autenticadas). */
public class HeaderComponent extends BasePage {

    private static final By USER_MENU_BTN = By.cssSelector("[data-testid='layout-topnav-user-menu-btn']");
    private static final By USER_LOGOUT_BTN = By.cssSelector("[data-testid='layout-topnav-user-logout-btn']");

    public HeaderComponent(WebDriver driver) {
        super(driver);
    }

    /** Encerra a sessão via menu do usuário. */
    public void logout() {
        click(USER_MENU_BTN);
        click(USER_LOGOUT_BTN);
        waitUrlContains("/login");
    }
}