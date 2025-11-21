package com.feedback.util;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility to add fields column to feedback_forms table
 */
public class DatabaseSchemaUpdater {
    public static void main(String[] args) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            
            // Add fields column if it doesn't exist
            String sql = "ALTER TABLE feedback_forms ADD COLUMN fields TEXT";
            
            try {
                statement.executeUpdate(sql);
                System.out.println("✅ Successfully added 'fields' column to feedback_forms table");
            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate column name")) {
                    System.out.println("ℹ️ 'fields' column already exists in feedback_forms table");
                } else {
                    System.out.println("❌ Error adding fields column: " + e.getMessage());
                }
            }
            
            statement.close();
            connection.close();
            
        } catch (Exception e) {
            System.err.println("❌ Database connection error: " + e.getMessage());
        }
    }
}
