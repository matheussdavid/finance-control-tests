package br.com.financecontrol.web.pages;

import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base dos Page Objects: encapsula waits explícitos e operações comuns.
 * Nenhum locator vive aqui — cada página define os seus.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    private final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.WAIT_TIMEOUT_SECONDS));
    }

    protected void navigate(String url) {
        driver.get(url);
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitClickable(locator).click();
    }

    protected void fill(By locator, String text) {
        WebElement input = waitVisible(locator);
        input.clear();
        input.sendKeys(text);
    }

    /**
     * Seleciona uma opção num select nativo disparando os eventos que o React
     * escuta ({@code change}/{@code input}) — o {@code Select} do Selenium não
     * garante isso em selects controlados.
     */
    protected void selectByValue(By locator, String value) {
        WebElement select = waitVisible(locator);
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        js.executeScript(
                "const el = arguments[0]; el.value = arguments[1];"
                        + " el.dispatchEvent(new Event('change', { bubbles: true }));"
                        + " el.dispatchEvent(new Event('input', { bubbles: true }));",
                select, value);
    }

    /**
     * Preenche um input {@code type="date"} (ou outro campo cujo valor não
     * aceita sendKeys) via JS, disparando os eventos que o React escuta.
     */
    protected void fillDate(By locator, String isoDate) {
        WebElement input = waitVisible(locator);
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        js.executeScript(
                "const el = arguments[0]; el.value = arguments[1];"
                        + " el.dispatchEvent(new Event('input', { bubbles: true }));"
                        + " el.dispatchEvent(new Event('change', { bubbles: true }));",
                input, isoDate);
    }

    protected String textOf(By locator) {
        return waitVisible(locator).getText();
    }

    protected boolean isVisible(By locator) {
        try {
            return waitVisible(locator).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Checagem rápida (2s) se o elemento está visível — evita espera cheia em polos. */
    protected boolean present(By locator) {
        try {
            WebDriverWait quick = new WebDriverWait(driver, Duration.ofSeconds(2));
            return quick.until(ExpectedConditions.visibilityOfElementLocated(locator)) != null;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void waitUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected void waitDisappear(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
}