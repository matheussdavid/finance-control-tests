package br.com.financecontrol.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Consultas à tabela {@code app_user}. */
public class UserRepository {

    public Optional<UserRow> findByEmail(String email) {
        String sql = "SELECT id, name, username, email, password_hash FROM app_user WHERE email = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UserRow(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível consultar o usuário no banco de dados", e);
        }

        return Optional.empty();
    }

    public record UserRow(String id, String name, String username, String email, String passwordHash) {
    }
}