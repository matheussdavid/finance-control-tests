package br.com.financecontrol.api.clients;

import br.com.financecontrol.api.requests.CategoryRequest;
import io.restassured.response.Response;

/** Endpoints de categorias (exigem token autenticado). */
public class CategoryClient extends ClientBase {

    public CategoryClient(String token) {
        super(token);
    }

    public Response create(CategoryRequest request) {
        return request()
                .body(request)
                .when()
                .post("/categories");
    }

    public Response list() {
        return request()
                .when()
                .get("/categories");
    }
}