package br.com.financecontrol.api.clients;

import br.com.financecontrol.api.requests.LoginRequest;
import br.com.financecontrol.api.requests.RegisterRequest;
import io.restassured.response.Response;

/** Endpoints públicos de autenticação. */
public class AuthClient extends ClientBase {

    public Response login(LoginRequest request) {
        return request()
                .body(request)
                .when()
                .post("/auth/login");
    }

    public Response login(Object body) {
        return request()
                .body(body)
                .when()
                .post("/auth/login");
    }

    public Response register(RegisterRequest request) {
        return request()
                .body(request)
                .when()
                .post("/auth/register");
    }

    public Response register(Object body) {
        return request()
                .body(body)
                .when()
                .post("/auth/register");
    }
}