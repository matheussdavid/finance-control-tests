package br.com.financecontrol.tests.api.autenticacao;

import br.com.financecontrol.api.clients.AccountClient;
import br.com.financecontrol.core.TestBase;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

/**
 * Testes do filtro global de autorização (regra #9 — sessão stateless).
 * {@code GET /accounts} é usado apenas como alvo: qualquer rota autenticada
 * serviria. Não é teste da feature contas (dedupe em contas-revisao.md).
 */
@Tag("api")
class AuthorizationApiTest extends TestBase {

    @Test
    @Tag("smoke")
    @DisplayName("Endpoint autenticado sem token deve retornar 401 UNAUTHORIZED")
    void deveRetornar401QuandoRequisicaoAutenticadaSemToken() {
        Response response = new AccountClient(null).list();

        response.then()
                .assertThat()
                .statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"))
                .body("message", equalTo("Autenticação necessária"));
    }

    @Test
    @DisplayName("Endpoint autenticado com token inválido deve retornar 401 UNAUTHORIZED")
    void deveRetornar401QuandoRequisicaoAutenticadaComTokenInvalido() {
        Response response = new AccountClient("token.forjado.invalido").list();

        response.then()
                .assertThat()
                .statusCode(401)
                .body("error", equalTo("UNAUTHORIZED"))
                .body("message", equalTo("Autenticação necessária"));
    }
}