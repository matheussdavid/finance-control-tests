package br.com.financecontrol.database;

import br.com.financecontrol.config.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Conexão JDBC com o PostgreSQL usado pela aplicação sob teste. */
public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                ConfigManager.DB_URL,
                ConfigManager.DB_USER,
                ConfigManager.DB_PASSWORD);
    }
}