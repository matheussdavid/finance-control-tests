package br.com.financecontrol.data;

import br.com.financecontrol.api.requests.RegisterRequest;

/**
 * Usuário de teste imutável: massa + referência das credenciais usadas.
 * Um teste usa este objeto para registrar (via fixture) e depois reutilizar
 * username/email/token sem "saber" como a massa foi gerada.
 */
public record TestUser(
        String name,
        String username,
        String email,
        String password,
        String userId,
        String token) {

    public static TestUser of(String name, String username, String email, String password) {
        return new TestUser(name, username, email, password, null, null);
    }

    public TestUser withCredentials(String userId, String token) {
        return new TestUser(name, username, email, password, userId, token);
    }

    public RegisterRequest toRegisterRequest() {
        return new RegisterRequest(name, username, email, password, password);
    }
}