package com.feedback.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.dao.UserDAO;
import com.feedback.model.User;
import com.feedback.util.JWTUtil;
import com.feedback.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Authentication servlet handling user registration and login
 */
@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AuthServlet.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(out, "Invalid endpoint");
                return;
            }
            
            switch (pathInfo) {
                case "/signup":
                    handleSignup(request, response, out);
                    break;
                case "/login":
                    handleLogin(request, response, out);
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendErrorResponse(out, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            logger.severe("Error in AuthServlet: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(out, "Internal server error");
        }
    }

    /**
     * Handle user registration
     */
    private void handleSignup(HttpServletRequest request, HttpServletResponse response, PrintWriter out) 
            throws IOException {
        
        logger.info("Signup request received");
        
        try {
            // Parse request body
            Map<String, Object> requestData = objectMapper.readValue(request.getReader(), Map.class);
            
            String name = (String) requestData.get("name");
            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");
            
            // Validate input
            if (name == null || email == null || password == null || 
                name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(out, "All fields are required");
                return;
            }
            
            if (!PasswordUtil.isValidPassword(password)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(out, "Password must be at least 6 characters long");
                return;
            }
            
            if (!PasswordUtil.isValidEmail(email)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(out, "Please enter a valid email address");
                return;
            }
            
            // Check if user already exists
            if (userDAO.userExists(email)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(out, "User already exists with this email");
                return;
            }
            
            // Create new user
            User user = new User(name.trim(), email.trim().toLowerCase(), PasswordUtil.hashPassword(password));
            
            if (userDAO.createUser(user)) {
                // Generate JWT token
                String token = JWTUtil.generateToken(user.getId(), user.getEmail());
                
                // Prepare response data
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", "User created successfully");
                responseData.put("token", token);
                
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("name", user.getName());
                userData.put("email", user.getEmail());
                userData.put("role", user.getRole());
                responseData.put("user", userData);
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println(objectMapper.writeValueAsString(responseData));
                logger.info("User created successfully: " + user.getEmail());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendErrorResponse(out, "Error creating user");
            }
            
        } catch (Exception e) {
            logger.severe("Error in signup: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(out, "Error creating user");
        }
    }

    /**
     * Handle user login
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response, PrintWriter out) 
            throws IOException {
        
        logger.info("Login request received");
        
        try {
            // Parse request body
            Map<String, Object> requestData = objectMapper.readValue(request.getReader(), Map.class);
            
            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");
            
            // Validate input
            if (email == null || password == null || 
                email.trim().isEmpty() || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(out, "Email and password are required");
                return;
            }
            
            // Find user by email
            User user = userDAO.findByEmail(email.trim().toLowerCase());
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                sendErrorResponse(out, "Invalid email or password");
                return;
            }
            
            // Verify password
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                sendErrorResponse(out, "Invalid email or password");
                return;
            }
            
            // Generate JWT token
            String token = JWTUtil.generateToken(user.getId(), user.getEmail());
            
            // Prepare response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "Login successful");
            responseData.put("token", token);
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole());
            responseData.put("user", userData);
            
            response.setStatus(HttpServletResponse.SC_OK);
            out.println(objectMapper.writeValueAsString(responseData));
            logger.info("Login successful for user: " + user.getEmail());
            
        } catch (Exception e) {
            logger.severe("Error in login: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(out, "Error logging in");
        }
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(PrintWriter out, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        try {
            out.println(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            logger.severe("Error sending error response: " + e.getMessage());
            out.println("{\"message\":\"" + message + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "GET method not supported");
        
        PrintWriter out = response.getWriter();
        out.println(objectMapper.writeValueAsString(errorResponse));
    }
}
