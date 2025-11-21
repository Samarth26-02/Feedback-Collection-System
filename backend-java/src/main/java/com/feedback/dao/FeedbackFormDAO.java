package com.feedback.dao;

import com.feedback.model.FeedbackForm;
import com.feedback.util.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object for FeedbackForm operations
 */
public class FeedbackFormDAO {
    private static final Logger logger = Logger.getLogger(FeedbackFormDAO.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a new feedback form
     */
    public boolean createForm(FeedbackForm form) {
        String sql = "INSERT INTO feedback_forms (title, description, created_by, created_at, updated_at, is_active, fields) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, form.getTitle());
            statement.setString(2, form.getDescription());
            statement.setInt(3, form.getCreatedBy());
            statement.setTimestamp(4, Timestamp.valueOf(form.getCreatedAt()));
            statement.setTimestamp(5, Timestamp.valueOf(form.getUpdatedAt()));
            statement.setBoolean(6, form.isActive());
            
            // Store fields as JSON
            String fieldsJson = null;
            if (form.getFields() != null && !form.getFields().isEmpty()) {
                try {
                    fieldsJson = objectMapper.writeValueAsString(form.getFields());
                } catch (Exception e) {
                    logger.warning("Error serializing fields to JSON: " + e.getMessage());
                }
            }
            statement.setString(7, fieldsJson);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        form.setId(generatedKeys.getInt(1));
                        logger.info("Feedback form created successfully with ID: " + form.getId() + " with " + (form.getFields() != null ? form.getFields().size() : 0) + " fields");
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error creating feedback form: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all forms by user ID
     */
    public List<FeedbackForm> getFormsByUserId(int userId) {
        List<FeedbackForm> forms = new ArrayList<>();
        String sql = "SELECT * FROM feedback_forms WHERE created_by = ? ORDER BY created_at DESC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    forms.add(mapResultSetToForm(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting forms by user ID: " + e.getMessage());
        }
        return forms;
    }

    /**
     * Get form by ID
     */
    public FeedbackForm getFormById(int formId) {
        String sql = "SELECT * FROM feedback_forms WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, formId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToForm(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting form by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update form
     */
    public boolean updateForm(FeedbackForm form) {
        String sql = "UPDATE feedback_forms SET title = ?, description = ?, updated_at = ?, is_active = ?, fields = ? WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, form.getTitle());
            statement.setString(2, form.getDescription());
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setBoolean(4, form.isActive());
            
            // Store fields as JSON
            String fieldsJson = null;
            if (form.getFields() != null && !form.getFields().isEmpty()) {
                try {
                    fieldsJson = objectMapper.writeValueAsString(form.getFields());
                } catch (Exception e) {
                    logger.warning("Error serializing fields to JSON: " + e.getMessage());
                }
            }
            statement.setString(5, fieldsJson);
            statement.setInt(6, form.getId());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("Error updating form: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete form
     */
    public boolean deleteForm(int formId) {
        String sql = "DELETE FROM feedback_forms WHERE id = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, formId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("Error deleting form: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all active forms
     */
    public List<FeedbackForm> getAllActiveForms() {
        List<FeedbackForm> forms = new ArrayList<>();
        String sql = "SELECT * FROM feedback_forms WHERE is_active = true ORDER BY created_at DESC";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    forms.add(mapResultSetToForm(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting all active forms: " + e.getMessage());
        }
        return forms;
    }

    /**
     * Map ResultSet to FeedbackForm object
     */
    private FeedbackForm mapResultSetToForm(ResultSet resultSet) throws SQLException {
        FeedbackForm form = new FeedbackForm();
        form.setId(resultSet.getInt("id"));
        form.setTitle(resultSet.getString("title"));
        form.setDescription(resultSet.getString("description"));
        form.setCreatedBy(resultSet.getInt("created_by"));
        form.setActive(resultSet.getBoolean("is_active"));
        
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            form.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            form.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Deserialize fields from JSON
        try {
            String fieldsJson = resultSet.getString("fields");
            if (fieldsJson != null && !fieldsJson.trim().isEmpty()) {
                List<com.feedback.model.FormField> fields = objectMapper.readValue(
                    fieldsJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, com.feedback.model.FormField.class)
                );
                form.setFields(fields);
            }
        } catch (Exception e) {
            logger.warning("Error deserializing fields from JSON: " + e.getMessage());
        }
        
        return form;
    }
}
