package br.com.financecontrol.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consultas à tabela {@code account}. */
public class AccountRepository {

    /** Tipos de conta ACTIVE que um usuário possui (para o seed criar só o que falta). */
    public List<String> findActiveTypesByUserId(String userId) {
        String sql = """
                SELECT DISTINCT type
                FROM account
                WHERE user_id = ? AND status = 'ACTIVE'
                """;

        List<String> types = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, java.util.UUID.fromString(userId));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    types.add(rs.getString("type"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Não foi possível consultar os tipos de conta no banco de dados", e);
        }

        return types;
    }

    public Optional<AccountRow> findByUserId(String userId) {
        String sql = """
                SELECT id, name, type, initial_balance, balance, status, created_at
                FROM account
                WHERE user_id = ?
                ORDER BY created_at
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, java.util.UUID.fromString(userId));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AccountRow(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getBigDecimal("initial_balance"),
                            rs.getBigDecimal("balance"),
                            rs.getString("status"),
                            rs.getObject("created_at", LocalDateTime.class)
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Não foi possível consultar uma conta no banco de dados", e);
        }

        return Optional.empty();
    }

    public record AccountRow(
            String id,
            String name,
            String type,
            BigDecimal initialBalance,
            BigDecimal balance,
            String status,
            LocalDateTime createdAt
    ) {
    }
}