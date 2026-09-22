package br.com.financecontrol.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/** Consultas à tabela {@code transaction} (com nome de conta e categoria). */
public class TransactionRepository {

    public Optional<TransactionRow> findByUserAndDescription(String userId, String description) {
        String sql = """
                SELECT t.id, t.type, t.description, t.amount, t.transaction_date,
                       a.name AS account_name, c.name AS category_name
                FROM "transaction" t
                JOIN account a ON a.id = t.account_id
                JOIN category c ON c.id = t.category_id
                WHERE t.user_id = ? AND t.description = ?
                ORDER BY t.created_at DESC
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, java.util.UUID.fromString(userId));
            statement.setString(2, description);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new TransactionRow(
                            rs.getString("id"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getBigDecimal("amount"),
                            rs.getDate("transaction_date").toLocalDate(),
                            rs.getString("account_name"),
                            rs.getString("category_name")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível consultar a transação no banco de dados", e);
        }

        return Optional.empty();
    }

    public record TransactionRow(
            String id,
            String type,
            String description,
            BigDecimal amount,
            LocalDate transactionDate,
            String accountName,
            String categoryName) {
    }
}