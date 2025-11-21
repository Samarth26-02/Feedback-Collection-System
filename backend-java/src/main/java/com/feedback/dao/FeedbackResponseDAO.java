package com.feedback.dao;

import com.feedback.model.FeedbackResponse;
import com.feedback.util.DatabaseConnection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Data Access Object for FeedbackResponse operations
 */
public class FeedbackResponseDAO {
    private static final Logger logger = Logger.getLogger(FeedbackResponseDAO.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create a new feedback response
     */
    public boolean createResponse(FeedbackResponse response) {
        String sql = "INSERT INTO feedback_responses (form_id, respondent_email, response_data, submitted_at) VALUES (?, ?, ?, ?)";
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, response.getFormId());
            statement.setString(2, response.getRespondentEmail());
            
            // Store response data as JSON
            String responseDataJson = objectMapper.writeValueAsString(response.getResponseData());
            statement.setString(3, responseDataJson);
            
            statement.setTimestamp(4, Timestamp.valueOf(response.getSubmittedAt()));
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        response.setId(generatedKeys.getInt(1));
                        logger.info("Feedback response created successfully with ID: " + response.getId());
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error creating feedback response: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Error serializing response data: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all responses for a specific form
     */
    public ResultSet getResponsesByFormId(int formId) throws SQLException {
        String sql = "SELECT * FROM feedback_responses WHERE form_id = ? ORDER BY submitted_at DESC";
        Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, formId);
        return statement.executeQuery();
    }
}

