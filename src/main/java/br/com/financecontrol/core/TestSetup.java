package br.com.financecontrol.core;

import br.com.financecontrol.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Inicialização única do Rest Assured (baseURI/parser).
 * Chamado pelas bases de teste ({@code TestBase} e {@code WebTestBase}) —
 * idempotente, seguro para múltiplas classes.
 */
public final class TestSetup {

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private TestSetup() {
    }

    public static synchronized void init() {
        if (INITIALIZED.get()) {
            return;
        }
        RestAssured.baseURI = ConfigManager.API_BASE_URL;
        RestAssured.defaultParser = Parser.JSON;
        INITIALIZED.set(true);
    }
}