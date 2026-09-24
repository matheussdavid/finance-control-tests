package br.com.financecontrol.api.clients;

import br.com.financecontrol.api.requests.AccountRequest;
import io.restassured.response.Response;

/** Endpoints de contas (exigem token autenticado). */
public class AccountClient extends ClientBase {

    public AccountClient(String token) {
        super(token);
    }

    public Response create(AccountRequest request) {
        return request()
                .body(request)
                .when()
                .post("/accounts");
    }

    public Response list() {
        return request()
                .when()
                .get("/accounts");
    }

    public Response getById(String id) {
        return request()
                .when()
                .get("/accounts/{id}", id);
    }
}