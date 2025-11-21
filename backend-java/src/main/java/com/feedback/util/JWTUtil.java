package com.feedback.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Utility class for JWT token operations
 */
public class JWTUtil {
    private static final Logger logger = Logger.getLogger(JWTUtil.class.getName());
    
    // JWT configuration - you should move these to a properties file in production
    private static final String SECRET_KEY = "feedbackCollectionSystemSecretKeyForJWTTokenGeneration12345";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * Generate JWT token for a user
     * @param userId User ID
     * @param email User email
     * @return JWT token string
     */
    public static String generateToken(int userId, String email) {
        try {
            Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
            
            String token = Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .claim("email", email)
                    .setIssuedAt(new Date())
                    .setExpiration(expirationDate)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
            
            logger.info("JWT token generated successfully for user: " + userId);
            return token;
        } catch (Exception e) {
            logger.severe("Error generating JWT token: " + e.getMessage());
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate JWT token and extract user ID
     * @param token JWT token string
     * @return User ID if token is valid, -1 otherwise
     */
    public static int validateTokenAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            int userId = Integer.parseInt(claims.getSubject());
            logger.info("JWT token validated successfully for user: " + userId);
            return userId;
        } catch (ExpiredJwtException e) {
            logger.warning("JWT token has expired: " + e.getMessage());
            return -1;
        } catch (UnsupportedJwtException e) {
            logger.warning("Unsupported JWT token: " + e.getMessage());
            return -1;
        } catch (MalformedJwtException e) {
            logger.warning("Malformed JWT token: " + e.getMessage());
            return -1;
        } catch (SecurityException e) {
            logger.warning("Invalid JWT signature: " + e.getMessage());
            return -1;
        } catch (IllegalArgumentException e) {
            logger.warning("JWT token is empty: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            logger.severe("Error validating JWT token: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Extract email from JWT token
     * @param token JWT token string
     * @return Email if token is valid, null otherwise
     */
    public static String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.get("email", String.class);
        } catch (Exception e) {
            logger.severe("Error extracting email from JWT token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if JWT token is expired
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            logger.warning("Error checking token expiration: " + e.getMessage());
            return true;
        }
    }
}
