package br.com.financecontrol.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Conta retornada pela API. Datas como {@code String} (ISO do app). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountResponse(
        String id,
        String name,
        String type,
        BigDecimal initialBalance,
        BigDecimal balance,
        String status,
        String createdAt,
        String updatedAt
) {
}