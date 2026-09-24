package br.com.financecontrol.tests.api.accounts;

import br.com.financecontrol.api.clients.AccountClient;
import br.com.financecontrol.api.models.AccountResponse;
import br.com.financecontrol.core.TestBase;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.database.AccountRepository;
import br.com.financecontrol.fixtures.SeedFixture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CT-013 — consulta por id: a massa de consulta é garantida pelo {@code SeedFixture}
 * antes dos testes; o banco apenas descobre a conta do usuário-catálogo logado.
 */
@Tag("api")
class GetAccountTest extends TestBase {

    private final SeedFixture seedFixture = new SeedFixture();
    private final AccountRepository accountRepository = new AccountRepository();

    private TestUser seed;

    @BeforeAll
    void seedContas() {
        seed = seedFixture.seedContas();
    }

    @Test
    @Tag("smoke")
    @DisplayName("Buscar conta por id retorna a conta")
    void deveRetornar200QuandoBuscarContaPorId() {
        AccountRepository.AccountRow row = accountRepository
                .findByUserId(seed.userId())
                .orElseThrow(() -> new AssertionError("massa de consulta ausente"));

        AccountResponse body = new AccountClient(seed.token())
                .getById(row.id())
                .then()
                .statusCode(200)
                .extract()
                .as(AccountResponse.class);

        assertThat(body.id()).isEqualTo(row.id());
        assertThat(body.name()).isEqualTo(row.name());
        assertThat(body.type()).isEqualTo(row.type());
        assertThat(body.initialBalance()).isEqualByComparingTo(row.initialBalance());
        assertThat(body.balance()).isEqualByComparingTo(row.balance());
        assertThat(body.status()).isEqualTo(row.status());
    }
}