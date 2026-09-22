package br.com.financecontrol.core;

import br.com.financecontrol.utils.ScreenshotUtil;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Encerra o driver ao fim de cada teste e, em caso de falha, captura
 * evidência (screenshot + URL + título) antes de encerrá-lo.
 * As callbacks do TestWatcher rodam depois dos {@code @AfterEach},
 * então o driver ainda está vivo quando {@code testFailed} captura a tela.
 */
public class WebTestWatcher implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        testInstance(context).ifPresent(test -> {
            if (test.driver != null) {
                String className = context.getTestClass().map(Class::getSimpleName).orElse("unknown");
                String methodName = context.getTestMethod().map(Method::getName).orElse("unknown");
                ScreenshotUtil.capture(test.driver, className, methodName);
                test.driver.quit();
            }
        });
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        quit(context);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        quit(context);
    }

    private void quit(ExtensionContext context) {
        testInstance(context).ifPresent(test -> {
            if (test.driver != null) {
                test.driver.quit();
            }
        });
    }

    private Optional<WebTestBase> testInstance(ExtensionContext context) {
        return context.getTestInstance().map(instance -> (WebTestBase) instance);
    }
}