package com.feedback.dao;

import com.feedback.model.User;
import com.feedback.util.DatabaseConnection;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Data Access Object for User operations
 */
public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    /**
     * Create a new user in the database
     * @param user User object to create
     * @return true if user was created successfully, false otherwise
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole());
            statement.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        logger.info("User created successfully with ID: " + user.getId());
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error creating user: " + e.getMessage());
            if (e.getErrorCode() == 1062) { // Duplicate entry error
                logger.warning("User with email " + user.getEmail() + " already exists");
            }
        }
        return false;
    }

    /**
     * Find user by email
     * @param email Email address to search for
     * @return User object if found, null otherwise
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find user by ID
     * @param id User ID to search for
     * @return User object if found, null otherwise
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error finding user by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if user exists by email
     * @param email Email address to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error checking if user exists: " + e.getMessage());
        }
        return false;
    }

    /**
     * Map ResultSet to User object
     * @param resultSet ResultSet from database query
     * @return User object
     * @throws SQLException if mapping fails
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return user;
    }
}
