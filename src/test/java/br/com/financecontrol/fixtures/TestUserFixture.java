package br.com.financecontrol.fixtures;

import br.com.financecontrol.api.clients.AuthClient;
import br.com.financecontrol.api.models.AuthResponse;
import br.com.financecontrol.builders.TestUserBuilder;
import br.com.financecontrol.data.TestUser;
import io.restassured.response.Response;

/**
 * Pré-condição padrão: registra um usuário novo e único via API.
 *
 * <p>O registro é estado de teste (não assert de negócio) — a falha aqui
 * apenas interrompe a pré-condição do teste.
 */
public class TestUserFixture {

    private final AuthClient authClient = new AuthClient();

    public TestUser registerUniqueUser() {
        TestUser user = TestUserBuilder.aUser().build();

        Response response = authClient.register(user.toRegisterRequest());
        response.then().statusCode(201);

        AuthResponse auth = response.then().extract().as(AuthResponse.class);
        return user.withCredentials(auth.user().id(), auth.token());
    }
}