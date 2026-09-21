package br.com.financecontrol.api.clients;

import br.com.financecontrol.api.models.ApiError;
import br.com.financecontrol.api.models.AuthResponse;
import br.com.financecontrol.api.requests.LoginRequest;
import br.com.financecontrol.api.requests.RegisterRequest;
import io.restassured.response.Response;

import static org.assertj.core.api.Assertions.assertThat;

/** Endpoints públicos de autenticação. */
public class AuthClient extends ClientBase {

    public Response login(LoginRequest request) {
        return request()
                .body(request)
                .when()
                .post("/auth/login");
    }

    public Response register(RegisterRequest request) {
        return request()
                .body(request)
                .when()
                .post("/auth/register");
    }

    /** Convinience: faz login e devolve o token (falha vira exceção de assert). */
    public String loginToken(LoginRequest request) {
        Response response = login(request);
        assertThat(response.statusCode())
                .as("login deveria retornar 200")
                .isEqualTo(200);
        return response.then().extract().as(AuthResponse.class).token();
    }

    /** Desserializa o erro padrão {@code ApiError} de uma resposta não-2xx. */
    public ApiError asError(Response response) {
        return response.then().extract().as(ApiError.class);
    }
}