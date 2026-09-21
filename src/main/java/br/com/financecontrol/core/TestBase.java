package br.com.financecontrol.core;

import org.junit.jupiter.api.BeforeAll;

/**
 * Classe base para testes de API.
 *
 * <p>Só garante a inicialização do Rest Assured. Todo o resto (clientes,
 * fixtures, builders) pertence às camadas de infraestrutura e é composto
 * pelos testes.
 */
public abstract class TestBase {

    @BeforeAll
    static void apiBaseSetup() {
        TestSetup.init();
    }
}