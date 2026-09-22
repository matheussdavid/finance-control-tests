package br.com.financecontrol.api.requests;

/** Payload de {@code POST /categories}. */
public record CategoryRequest(String name, String type) {
}