package br.com.financecontrol.api.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/** Corpo padrão de erro da API (formato {@code ApiError} do backend). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fields) {

    /** Mantém compatibilidade quando o backend omite {@code fields} (JSON omite nulls). */
    public ApiError {
        fields = fields == null ? Map.of() : fields;
    }
}