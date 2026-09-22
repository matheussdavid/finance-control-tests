package br.com.financecontrol.fixtures;

import br.com.financecontrol.api.clients.AccountClient;
import br.com.financecontrol.api.clients.CategoryClient;
import br.com.financecontrol.api.models.AccountResponse;
import br.com.financecontrol.api.models.CategoryResponse;
import br.com.financecontrol.api.requests.AccountRequest;
import br.com.financecontrol.api.requests.CategoryRequest;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.data.UserFaker;

import java.math.BigDecimal;

/**
 * Pré-condição para cenários financeiros: registra um usuário e cria a
 * estrutura mínima para movimentar dinheiro (conta CHECKING + categoria
 * EXPENSE). Usado pelos testes de persistência e E2E.
 */
public class FinanceFixture {

    public record FinanceContext(TestUser user, AccountResponse account, CategoryResponse category) {
    }

    private final TestUserFixture userFixture = new TestUserFixture();

    public FinanceContext createExpenseContext() {
        TestUser user = userFixture.registerUniqueUser();

        AccountResponse account = new AccountClient(user.token())
                .create(new AccountRequest(UserFaker.accountName(), "CHECKING", BigDecimal.ZERO))
                .then()
                .statusCode(201)
                .extract()
                .as(AccountResponse.class);

        CategoryResponse category = new CategoryClient(user.token())
                .create(new CategoryRequest(UserFaker.categoryName(), "EXPENSE"))
                .then()
                .statusCode(201)
                .extract()
                .as(CategoryResponse.class);

        return new FinanceContext(user, account, category);
    }
}