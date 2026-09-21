package br.com.financecontrol.driver;

import org.openqa.selenium.WebDriver;

import java.util.Objects;

/**
 * Guarda o WebDriver da thread atual (ThreadLocal) para que execução paralela
 * futura seja viável sem compartilhar estado entre testes.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    /** Abre (ou reutiliza) o driver da thread atual criado conforme config. */
    public static WebDriver open() {
        WebDriver current = DRIVER.get();
        if (current != null) {
            return current;
        }
        WebDriver driver = DriverFactory.create();
        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver get() {
        return DRIVER.get();
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}