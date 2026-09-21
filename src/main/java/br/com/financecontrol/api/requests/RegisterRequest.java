package br.com.financecontrol.api.requests;

/** Payload de {@code POST /auth/register}. */
public record RegisterRequest(
        String name,
        String username,
        String email,
        String password,
        String confirmPassword) {
}