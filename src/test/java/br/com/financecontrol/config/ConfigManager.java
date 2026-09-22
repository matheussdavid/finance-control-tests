package br.com.financecontrol.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Fonte única de configuração da automação.
 *
 * <p>Precedência de cada chave:
 * <ol>
 *   <li>variável de ambiente (System.getenv)</li>
 *   <li>arquivo {@code .env} na raiz do projeto (dotenv)</li>
 *   <li>valor padrão seguro definido aqui</li>
 * </ol>
 */
public final class ConfigManager {

    private static final Dotenv DOTENV =
            Dotenv.configure().ignoreIfMissing().load();

    private ConfigManager() {
    }

    // Chaves lidas em .env / variáveis de ambiente
    public static final String BASE_URL = valueOf("BASE_URL", "http://localhost:5173");
    public static final String API_BASE_URL = valueOf("API_BASE_URL", "http://localhost:8080");
    public static final String DB_URL = valueOf("DB_URL", "jdbc:postgresql://localhost:5432/finance_control");
    public static final String DB_USER = valueOf("DB_USER", "finance");
    public static final String DB_PASSWORD = valueOf("DB_PASSWORD", "finance_pass");

    /**
     * Caminho opcional para o binário do Chrome (usado quando o Chrome não
     * está instalado em local padrão do sistema — ex. dev sem sudo ou CI custom).
     */
    public static final String CHROME_BINARY = valueOf("CHROME_BINARY", "");

    public static final boolean HEADLESS = Boolean.parseBoolean(valueOf("HEADLESS", "true"));
    public static final int WAIT_TIMEOUT_SECONDS = Integer.parseInt(valueOf("WAIT_TIMEOUT_SECONDS", "15"));
    public static final String SCREENSHOT_DIR = valueOf("SCREENSHOT_DIR", "target/screenshots");

    /**
     * Senha sintética padrão para usuários criados em teste.
     * Não é um secret real — o próprio teste gera o usuário com esta senha.
     */
    public static final String DEFAULT_PASSWORD = valueOf("DEFAULT_PASSWORD", "Passw0rd!123");

    private static String valueOf(String key, String defaultValue) {
        String env = System.getenv(key);
        if (env != null) {
            return env;
        }
        String dotenv = DOTENV.get(key);
        return dotenv != null ? dotenv : defaultValue;
    }
}