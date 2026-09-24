package br.com.financecontrol.fixtures;

import br.com.financecontrol.api.clients.AccountClient;
import br.com.financecontrol.api.clients.AuthClient;
import br.com.financecontrol.api.models.AuthResponse;
import br.com.financecontrol.api.requests.AccountRequest;
import br.com.financecontrol.api.requests.LoginRequest;
import br.com.financecontrol.config.ConfigManager;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.database.AccountRepository;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Massa de banco dos testes de consulta — um arquivo, um método por função.
 *
 * <p>O banco informa o que já existe; a API cria apenas o que falta.
 * Usuário-catálogo com credenciais fixas (SEEDED_ACCOUNT_*), idempotente:
 * em execuções seguintes ele reutiliza usuário e contas já criados.
 */
public class SeedFixture {

    private static final String SEED_NAME = "Contas Seed";
    private static final String SEED_USERNAME = "seed_contas";
    private static final List<String> ACCOUNT_TYPES = List.of("CHECKING", "SAVINGS", "CASH");

    private final AuthClient authClient = new AuthClient();
    private final AccountRepository accountRepository = new AccountRepository();

    /**
     * Garante o usuário-catálogo logado com 1 conta ACTIVE de cada tipo.
     * Futuros seeds de outras funções ficam no mesmo arquivo (seedTransacoes, ...).
     */
    public TestUser seedContas() {
        TestUser user = sessionUser();           // 1) garante o usuário logado (login | register)
        ensureAccountsOfEveryType(user);         // 2) garante uma conta de cada tipo
        return user;
    }

    /**
     * Tenta logar com as credenciais fixas. Se falhar, registra o usuário via API.
     * Se o register retornar 409, o usuário existe mas a senha divergiu (erro de configuração).
     */
    private TestUser sessionUser() {
        Response login = authClient.login(LoginRequest.of(
                ConfigManager.SEEDED_ACCOUNT_EMAIL, ConfigManager.SEEDED_ACCOUNT_PASSWORD));

        if (login.statusCode() == 200) {
            return withCredentials(login);
        }

        Response register = authClient.register(seedUser().toRegisterRequest());
        if (register.statusCode() == 201) {
            return withCredentials(register);
        }
        if (register.statusCode() == 409) {
            throw new IllegalStateException(
                    "Usuário-catálogo existe mas o login falhou: SEEDED_ACCOUNT_PASSWORD divergente");
        }
        throw new IllegalStateException("Falha ao preparar usuário-catálogo: HTTP " + register.statusCode());
    }

    private TestUser seedUser() {
        return TestUser.of(
                SEED_NAME,
                SEED_USERNAME,
                ConfigManager.SEEDED_ACCOUNT_EMAIL,
                ConfigManager.SEEDED_ACCOUNT_PASSWORD);
    }

    private TestUser withCredentials(Response response) {
        AuthResponse auth = response.then().extract().as(AuthResponse.class);
        return seedUser().withCredentials(auth.user().id(), auth.token());
    }

    /**
     * Pergunta ao banco quais tipos ACTIVE o catálogo já tem e cria via API
     * somente os ausentes — assim a massa não duplica entre execuções.
     */
    private void ensureAccountsOfEveryType(TestUser user) {
        List<String> existingTypes = accountRepository.findActiveTypesByUserId(user.userId());

        for (String type : ACCOUNT_TYPES) {
            if (!existingTypes.contains(type)) {
                createAccount(user, type);
            }
        }
    }

    private void createAccount(TestUser user, String type) {
        AccountRequest request = new AccountRequest("Seed " + type, type, BigDecimal.ZERO);
        new AccountClient(user.token())
                .create(request)
                .then()
                .statusCode(201);
    }
}