package br.com.financecontrol.tests.api;

import br.com.financecontrol.api.clients.TransactionClient;
import br.com.financecontrol.api.requests.TransactionRequest;
import br.com.financecontrol.core.TestBase;
import br.com.financecontrol.data.UserFaker;
import br.com.financecontrol.database.TransactionRepository;
import br.com.financecontrol.fixtures.FinanceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validação de persistência: a API cria uma despesa e o teste confere no
 * PostgreSQL que a transação foi gravada com os dados corretos.
 */
@Tag("api")
class TransactionPersistenceTest extends TestBase {

    private final FinanceFixture financeFixture = new FinanceFixture();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    @Test
    @DisplayName("Despesa criada via API persiste no PostgreSQL com dados corretos")
    void devePersistirDespesaCriadaViaApi() {
        FinanceFixture.FinanceContext context = financeFixture.createExpenseContext();
        BigDecimal amount = UserFaker.transactionAmount();
        String description = UserFaker.transactionDescription();
        LocalDate today = LocalDate.now();

        TransactionClient transactions = new TransactionClient(context.user().token());
        var response = transactions.create(new TransactionRequest(
                "EXPENSE",
                description,
                amount,
                context.account().id(),
                context.category().id(),
                today.toString()));
        response.then().statusCode(201);

        var row = transactionRepository
                .findByUserAndDescription(context.user().userId(), description)
                .orElseThrow(() -> new AssertionError("Transação não encontrada no banco"));

        assertThat(row.type()).isEqualTo("EXPENSE");
        assertThat(row.amount()).isEqualByComparingTo(amount);
        assertThat(row.transactionDate()).isEqualTo(today);
        assertThat(row.accountName()).isEqualTo(context.account().name());
        assertThat(row.categoryName()).isEqualTo(context.category().name());
    }
}