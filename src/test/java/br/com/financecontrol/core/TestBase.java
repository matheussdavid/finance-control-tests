package br.com.financecontrol.core;

import br.com.financecontrol.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

/**
 * Classe base dos testes de API: configura o Rest Assured uma única vez.
 * Cada teste compõe os clients/fixtures que precisa.
 */
public abstract class TestBase {

    protected TestBase() {
        RestAssured.baseURI = ConfigManager.API_BASE_URL;
        RestAssured.defaultParser = Parser.JSON;
    }
}