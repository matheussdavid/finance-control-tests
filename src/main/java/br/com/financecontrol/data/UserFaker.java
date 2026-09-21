package br.com.financecontrol.data;

import br.com.financecontrol.config.ConfigManager;
import net.datafaker.Faker;

import java.util.Locale;

/** Geração de dados dinâmicos/únicos para massa de teste. */
public final class UserFaker {

    private static final Faker FAKER = new Faker(new Locale("pt-BR"));

    private UserFaker() {
    }

    public static String username() {
        return "user_" + FAKER.regexify("[a-z0-9]{6}");
    }

    public static String email() {
        return "qa_" + FAKER.regexify("[a-z0-9]{8}") + "@example.com";
    }

    public static String fullName() {
        return FAKER.name().fullName();
    }

    /** Senha sintética padrão dos usuários de teste (não é secret real). */
    public static String password() {
        return ConfigManager.DEFAULT_PASSWORD;
    }
}