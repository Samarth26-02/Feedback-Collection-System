package com.feedback.test;

import com.feedback.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple test class to verify database connection
 */
public class DatabaseTest {
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try {
            // Test connection
            Connection connection = DatabaseConnection.getConnection();
            System.out.println("✅ Database connection successful!");
            
            // Test query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) as user_count FROM users");
            
            if (resultSet.next()) {
                int userCount = resultSet.getInt("user_count");
                System.out.println("✅ Database query successful! Found " + userCount + " users in database.");
            }
            
            connection.close();
            System.out.println("✅ Database connection test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
