package br.com.financecontrol.driver;

import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

/**
 * Cria o WebDriver (Chrome). O driver binário é resolvido automaticamente
 * pelo Selenium Manager (embutido no Selenium 4) — sem dependência extra.
 * Headless é controlado por {@code HEADLESS} (true no CI).
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        ChromeOptions options = new ChromeOptions();
        if (!ConfigManager.CHROME_BINARY.isBlank()) {
            options.setBinary(ConfigManager.CHROME_BINARY);
        }
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--lang=pt-BR",
                "--disable-notifications");
        if (ConfigManager.HEADLESS) {
            options.addArguments("--headless=new");
        }
        options.setExperimentalOption("prefs",
                Map.of("intl.accept_languages", "pt-BR,pt,en-US"));
        return new ChromeDriver(options);
    }
}