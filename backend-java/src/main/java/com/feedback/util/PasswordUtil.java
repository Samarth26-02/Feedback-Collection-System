package com.feedback.util;

import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;

/**
 * Utility class for password hashing and verification using BCrypt
 */
public class PasswordUtil {
    private static final Logger logger = Logger.getLogger(PasswordUtil.class.getName());
    
    // BCrypt work factor - higher values are more secure but slower
    private static final int WORK_FACTOR = 10;

    /**
     * Hash a plain text password
     * @param plainPassword Plain text password
     * @return Hashed password
     */
    public static String hashPassword(String plainPassword) {
        try {
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
            logger.info("Password hashed successfully");
            return hashedPassword;
        } catch (Exception e) {
            logger.severe("Error hashing password: " + e.getMessage());
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword Plain text password to verify
     * @param hashedPassword Hashed password from database
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            boolean isValid = BCrypt.checkpw(plainPassword, hashedPassword);
            logger.info("Password verification result: " + isValid);
            return isValid;
        } catch (Exception e) {
            logger.severe("Error verifying password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a password meets minimum requirements
     * @param password Password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }

    /**
     * Check if an email is valid
     * @param email Email to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Basic email validation regex
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
