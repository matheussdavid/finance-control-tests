package br.com.financecontrol.api.models;

/** Usuário devolvido dentro do {@code AuthResponse}. */
public record UserInfo(String id, String name, String email) {
}