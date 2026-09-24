package br.com.financecontrol.fixtures;

import br.com.financecontrol.api.clients.AccountClient;
import br.com.financecontrol.api.models.AccountResponse;
import br.com.financecontrol.api.requests.AccountRequest;
import br.com.financecontrol.data.TestUser;
import br.com.financecontrol.data.UserFaker;

import java.math.BigDecimal;

/**
 * Massa própria dos testes de contas (verbos de mutação): cada teste cria suas
 * contas via API com o token do usuário gerado na hora. A massa de consulta
 * vive no {@link SeedFixture}.
 */
public class AccountsFixture {

    public record AccountContext(TestUser user, AccountResponse account) {
    }

    private final TestUserFixture userFixture = new TestUserFixture();

    public AccountContext createAccountContext() {
        TestUser user = userFixture.registerUniqueUser();

        AccountResponse account = new AccountClient(user.token())
                .create(new AccountRequest(UserFaker.accountName(), "CHECKING", BigDecimal.ZERO))
                .then()
                .statusCode(201)
                .extract()
                .as(AccountResponse.class);

        return new AccountContext(user, account);
    }
}