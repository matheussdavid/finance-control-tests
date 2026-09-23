package br.com.financecontrol.web.pages;

import br.com.financecontrol.api.requests.RegisterRequest;
import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RegisterPage extends BasePage {

    private static final By PAGE = By.cssSelector("[data-testid='login-page']");
    private static final By NAME_INPUT = By.cssSelector("[data-testid='register-name-input']");
    private static final By USERNAME_INPUT = By.cssSelector("[data-testid='register-username-input']");
    private static final By EMAIL_INPUT = By.cssSelector("[data-testid='register-email-input']");
    private static final By PASSWORD_INPUT = By.cssSelector("[data-testid='login-password-input']");
    private static final By CONFIRM_PASSWORD_INPUT = By.cssSelector("[data-testid='register-confirm-password-input']");
    private static final By REGISTER_BTN = By.cssSelector("[data-testid='login-submit-btn']");
    private static final By ERROR_MESSAGE = By.cssSelector("[data-testid='message-error']");

    public enum RegisterField {
        NAME(NAME_INPUT),
        USERNAME(USERNAME_INPUT),
        EMAIL(EMAIL_INPUT),
        PASSWORD(PASSWORD_INPUT),
        CONFIRM_PASSWORD(CONFIRM_PASSWORD_INPUT);

        private final By locator;

        RegisterField(By locator) {
            this.locator = locator;
        }

        public By locator() {
            return locator;
        }
    }

    public RegisterPage(WebDriver driver) { super(driver);}

    public RegisterPage open() {
        navigate(ConfigManager.BASE_URL + "/register");
        waitVisible(PAGE);
        return this;
    }

    public boolean isDisplayed() {
        return isVisible(PAGE);
    }

    public RegisterPage fillValidData(RegisterRequest request) {
        fill(NAME_INPUT, request.name());
        fill(USERNAME_INPUT, request.username());
        fill(EMAIL_INPUT, request.email());
        fill(PASSWORD_INPUT, request.password());
        fill(CONFIRM_PASSWORD_INPUT, request.confirmPassword());
        return this;
    }

    public RegisterPage submit() {
        click(REGISTER_BTN);
        return this;
    }

    public RegisterPage register(RegisterRequest request) {
        fillValidData(request).submit();
        return this;
    }

    public String getErrorMessage() {
        return waitVisible(ERROR_MESSAGE).getText();
    }

    public RegisterPage clearField(RegisterField field){
        WebElement input = waitVisible(field.locator());
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        return this;
    }
}
