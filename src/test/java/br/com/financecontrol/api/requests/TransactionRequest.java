package br.com.financecontrol.api.requests;

import java.math.BigDecimal;

/** Payload de {@code POST /transactions}. */
public record TransactionRequest(
        String type,
        String description,
        BigDecimal amount,
        String accountId,
        String categoryId,
        String transactionDate) {
}