package br.com.financecontrol.api.clients;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Base dos API Clients: monta a requisição Rest Assured já com JSON e o
 * header de autorização quando o client é construído com um token autenticado.
 */
public abstract class ClientBase {

    private final String token;

    protected ClientBase() {
        this(null);
    }

    protected ClientBase(String token) {
        this.token = token;
    }

    protected RequestSpecification request() {
        RequestSpecification spec = RestAssured.given().contentType(ContentType.JSON);
        if (token != null) {
            spec = spec.header("Authorization", "Bearer " + token);
        }
        return spec;
    }
}