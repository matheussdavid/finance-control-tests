package br.com.financecontrol.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Categoria criada — {@code id}, {@code name} e {@code type} identificam o select. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryResponse(String id, String name, String type) {
}