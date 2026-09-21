package br.com.financecontrol.api.models;

/** Resposta padrão de autenticação: {@code {token, user}}. */
public record AuthResponse(String token, UserInfo user) {
}