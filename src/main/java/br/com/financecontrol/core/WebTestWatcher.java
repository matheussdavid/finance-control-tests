package br.com.financecontrol.core;

import br.com.financecontrol.driver.DriverManager;
import br.com.financecontrol.utils.ScreenshotUtil;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registra evidência de falha (screenshot + URL) e encerra o driver ao final
 * de cada teste.
 *
 * <p>O encerramento fica aqui (e não no {@code @AfterEach}) porque os métodos
 * do {@code TestWatcher} rodam depois dos {@code @AfterEach} — assim o driver
 * continua vivo quando {@code testFailed} precisa capturar a evidência.
 */
public class WebTestWatcher implements TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(WebTestWatcher.class);

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        captureEvidence(context);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        DriverManager.quit();
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        DriverManager.quit();
    }

    private void captureEvidence(ExtensionContext context) {
        WebDriver driver = DriverManager.get();
        if (driver != null) {
            ScreenshotUtil.capture(driver,
                    context.getTestClass().map(Class::getSimpleName).orElse("unknown"),
                    context.getTestMethod().map(java.lang.reflect.Method::getName).orElse("unknown"));
        } else {
            log.error("Falha em teste sem driver ativo: {}", context.getDisplayName());
        }
        DriverManager.quit();
    }
}