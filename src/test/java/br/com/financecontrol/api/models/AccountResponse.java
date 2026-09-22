package br.com.financecontrol.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Conta criada — {@code id} e {@code name} identificam a conta nos selects. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountResponse(String id, String name) {
}