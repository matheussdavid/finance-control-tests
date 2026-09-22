package br.com.financecontrol.api.requests;

import java.math.BigDecimal;

/** Payload de {@code POST /accounts}. */
public record AccountRequest(String name, String type, BigDecimal initialBalance) {
}