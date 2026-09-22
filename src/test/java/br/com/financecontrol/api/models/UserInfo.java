package br.com.financecontrol.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Usuário devolvido dentro do {@code AuthResponse}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInfo(String id, String name, String email) {
}