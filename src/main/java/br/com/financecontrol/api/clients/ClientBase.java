package br.com.financecontrol.api.clients;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Base comum dos clients REST. Já carrega o content-type JSON; o
 * {@code baseURI} é definido pelo {@code TestSetup}.
 */
public abstract class ClientBase {

    protected RequestSpecification request() {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
}