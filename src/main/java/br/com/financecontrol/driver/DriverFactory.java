package br.com.financecontrol.driver;

import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.Map;

/**
 * Criação do WebDriver. O driver binário é resolvido automaticamente pelo
 * Selenium Manager (embutido no Selenium 4) — sem dependência extra.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create(Browser browser, boolean headless) {
        return switch (browser) {
            case CHROME -> new ChromeDriver(chromeOptions(headless));
            case FIREFOX -> new FirefoxDriver(firefoxOptions(headless));
            case EDGE -> throw new UnsupportedOperationException("Edge ainda não configurado; siga o padrão do ChromeOptions.");
        };
    }

    public static WebDriver create() {
        Browser browser = Browser.fromConfig(ConfigManager.BROWSER);
        return create(browser, ConfigManager.HEADLESS);
    }

    private static ChromeOptions chromeOptions(boolean headless) {
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
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.setExperimentalOption("prefs",
                Map.of("intl.accept_languages", "pt-BR,pt,en-US"));
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--no-sandbox", "--window-size=1920,1080");
        if (headless) {
            options.addArguments("--headless");
        }
        return options;
    }
}