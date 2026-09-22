package br.com.financecontrol.api.clients;

import br.com.financecontrol.api.requests.TransactionRequest;
import io.restassured.response.Response;

/** Endpoints de transações (exigem token autenticado). */
public class TransactionClient extends ClientBase {

    public TransactionClient(String token) {
        super(token);
    }

    public Response create(TransactionRequest request) {
        return request()
                .body(request)
                .when()
                .post("/transactions");
    }

    public Response list() {
        return request()
                .when()
                .get("/transactions");
    }
}