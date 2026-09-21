package br.com.financecontrol.driver;

import java.util.Arrays;

/** Navegadores suportados. Estrutura pronta para expansão (firefox/edge). */
public enum Browser {

    CHROME("chrome"),
    FIREFOX("firefox"),
    EDGE("edge");

    private final String id;

    Browser(String id) {
        this.id = id;
    }

    public static Browser fromConfig(String value) {
        return Arrays.stream(values())
                .filter(b -> b.id.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Navegador inválido: '%s'. Use um de: chrome, firefox, edge".formatted(value)));
    }
}