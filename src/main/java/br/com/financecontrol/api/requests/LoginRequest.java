package br.com.financecontrol.api.requests;

/** Payload de {@code POST /auth/login}: identifier pode ser username ou email. */
public record LoginRequest(String identifier, String password) {

    public static LoginRequest of(String identifier, String password) {
        return new LoginRequest(identifier, password);
    }
}