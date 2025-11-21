package com.feedback.server;

import com.feedback.dao.UserDAO;
import com.feedback.dao.FeedbackFormDAO;
import com.feedback.model.User;
import com.feedback.model.FeedbackForm;
import com.feedback.model.FormField;
import com.feedback.util.DatabaseConnection;
import com.feedback.util.JWTUtil;
import com.feedback.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Simple HTTP server for the Feedback Collection System
 */
public class SimpleServer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final UserDAO userDAO = new UserDAO();
    private static final FeedbackFormDAO formDAO = new FeedbackFormDAO();
    private static final com.feedback.dao.FeedbackResponseDAO responseDAO = new com.feedback.dao.FeedbackResponseDAO();
    
    static {
        // Configure ObjectMapper to handle Java 8 time types
        objectMapper.findAndRegisterModules();
        // Configure date format to ISO-8601
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Starting Feedback Collection System Server...");
        
        // Test database connection first
        try {
            DatabaseConnection.testConnection();
            System.out.println("✅ Database connection successful!");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("Please make sure MySQL is running and the database 'feedbacksystemdb' exists.");
            return;
        }
        
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Add endpoints
        server.createContext("/api/auth/signup", new AuthHandler("signup"));
        server.createContext("/api/auth/login", new AuthHandler("login"));
        server.createContext("/api/health", new HealthHandler());
        
        // Form management endpoints
        server.createContext("/api/forms", new FormHandler());
        server.createContext("/api/forms/", new FormHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🚀 Server started successfully!");
        System.out.println("📍 Backend API: http://localhost:8080/api");
        System.out.println("🔍 Health Check: http://localhost:8080/api/health");
        System.out.println("📝 Auth Endpoints:");
        System.out.println("   - POST http://localhost:8080/api/auth/signup");
        System.out.println("   - POST http://localhost:8080/api/auth/login");
        System.out.println();
        System.out.println("Press Ctrl+C to stop the server");
        
        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Shutting down server...");
            server.stop(0);
            System.out.println("✅ Server stopped successfully!");
        }));
    }
    
    /**
     * Health check handler
     */
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"ok\",\"message\":\"Server is running\"}";
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    /**
     * Authentication handler
     */
    static class AuthHandler implements HttpHandler {
        private final String endpoint;
        
        public AuthHandler(String endpoint) {
            this.endpoint = endpoint;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            
            // Handle OPTIONS request for CORS preflight
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, "Method not allowed", 405);
                return;
            }
            
            try {
                if ("signup".equals(endpoint)) {
                    handleSignup(exchange);
                } else if ("login".equals(endpoint)) {
                    handleLogin(exchange);
                } else {
                    sendErrorResponse(exchange, "Endpoint not found", 404);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, "Internal server error", 500);
            }
        }
        
        private void handleSignup(HttpExchange exchange) throws IOException {
            // Parse request body
            String requestBody = readRequestBody(exchange);
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            String name = (String) requestData.get("name");
            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");
            
            // Validate input
            if (name == null || email == null || password == null || 
                name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                sendErrorResponse(exchange, "All fields are required", 400);
                return;
            }
            
            if (!PasswordUtil.isValidPassword(password)) {
                sendErrorResponse(exchange, "Password must be at least 6 characters long", 400);
                return;
            }
            
            if (!PasswordUtil.isValidEmail(email)) {
                sendErrorResponse(exchange, "Please enter a valid email address", 400);
                return;
            }
            
            // Check if user already exists
            if (userDAO.userExists(email)) {
                sendErrorResponse(exchange, "User already exists with this email", 400);
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
                
                sendSuccessResponse(exchange, responseData, 201);
                System.out.println("✅ User created successfully: " + user.getEmail());
            } else {
                sendErrorResponse(exchange, "Error creating user", 500);
            }
        }
        
        private void handleLogin(HttpExchange exchange) throws IOException {
            // Parse request body
            String requestBody = readRequestBody(exchange);
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");
            
            // Validate input
            if (email == null || password == null || 
                email.trim().isEmpty() || password.trim().isEmpty()) {
                sendErrorResponse(exchange, "Email and password are required", 400);
                return;
            }
            
            // Find user by email
            User user = userDAO.findByEmail(email.trim().toLowerCase());
            if (user == null) {
                sendErrorResponse(exchange, "Invalid email or password", 401);
                return;
            }
            
            // Verify password
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                sendErrorResponse(exchange, "Invalid email or password", 401);
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
            
            sendSuccessResponse(exchange, responseData, 200);
            System.out.println("✅ Login successful for user: " + user.getEmail());
        }
        
        private String readRequestBody(HttpExchange exchange) throws IOException {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            return body.toString();
        }
        
        private void sendSuccessResponse(HttpExchange exchange, Object data, int statusCode) throws IOException {
            String response = objectMapper.writeValueAsString(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private void sendErrorResponse(HttpExchange exchange, String message, int statusCode) throws IOException {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", message);
            String response = objectMapper.writeValueAsString(errorResponse);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private void setCORSHeaders(HttpExchange exchange) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
    }
    
    /**
     * Form management handler
     */
    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            
            // Handle OPTIONS request for CORS preflight
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                return;
            }
            
            try {
                String path = exchange.getRequestURI().getPath();
                String method = exchange.getRequestMethod();
                
                if (path.equals("/api/forms") || path.equals("/api/forms/")) {
                    if ("GET".equals(method)) {
                        handleGetForms(exchange);
                    } else if ("POST".equals(method)) {
                        handleCreateForm(exchange);
                    } else {
                        sendErrorResponse(exchange, "Method not allowed", 405);
                    }
                } else if (path.startsWith("/api/forms/")) {
                    String afterForms = path.substring("/api/forms/".length());
                    if (afterForms.contains("/")) {
                        String[] parts = afterForms.split("/");
                        try {
                            int formId = Integer.parseInt(parts[0]);
                            if (parts.length > 1 && "submit".equals(parts[1]) && "POST".equals(method)) {
                                handleSubmitForm(exchange, formId);
                            } else {
                                sendErrorResponse(exchange, "Invalid endpoint", 404);
                            }
                        } catch (NumberFormatException e) {
                            sendErrorResponse(exchange, "Invalid form ID", 400);
                        }
                    } else if (!afterForms.isEmpty()) {
                        try {
                            int formId = Integer.parseInt(afterForms);
                            if ("GET".equals(method)) {
                                handleGetForm(exchange, formId);
                            } else if ("PUT".equals(method)) {
                                handleUpdateForm(exchange, formId);
                            } else if ("DELETE".equals(method)) {
                                handleDeleteForm(exchange, formId);
                            } else {
                                sendErrorResponse(exchange, "Method not allowed", 405);
                            }
                        } catch (NumberFormatException e) {
                            sendErrorResponse(exchange, "Invalid form ID", 400);
                        }
                    } else {
                        sendErrorResponse(exchange, "Form ID required", 400);
                    }
                } else {
                    sendErrorResponse(exchange, "Endpoint not found", 404);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, "Internal server error", 500);
            }
        }
        
        private void handleGetForms(HttpExchange exchange) throws IOException {
            // Extract user ID from JWT token
            int userId = extractUserIdFromRequest(exchange);
            if (userId == -1) {
                sendErrorResponse(exchange, "Unauthorized - Invalid or missing token", 401);
                return;
            }
            
            // Get only forms created by the authenticated user
            List<FeedbackForm> forms = formDAO.getFormsByUserId(userId);
            sendSuccessResponse(exchange, forms, 200);
        }
        
        private void handleGetForm(HttpExchange exchange, int formId) throws IOException {
            // Extract user ID from JWT token
            int userId = extractUserIdFromRequest(exchange);
            if (userId == -1) {
                sendErrorResponse(exchange, "Unauthorized - Invalid or missing token", 401);
                return;
            }
            
            FeedbackForm form = formDAO.getFormById(formId);
            if (form != null) {
                // Check if the form belongs to the authenticated user
                if (form.getCreatedBy() != userId) {
                    sendErrorResponse(exchange, "Unauthorized - You don't have access to this form", 403);
                    return;
                }
                sendSuccessResponse(exchange, form, 200);
            } else {
                sendErrorResponse(exchange, "Form not found", 404);
            }
        }
        
        private void handleCreateForm(HttpExchange exchange) throws IOException {
            // Extract user ID from JWT token
            int userId = extractUserIdFromRequest(exchange);
            if (userId == -1) {
                sendErrorResponse(exchange, "Unauthorized - Invalid or missing token", 401);
                return;
            }
            
            String requestBody = readRequestBody(exchange);
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            List<Map<String, Object>> fieldsData = (List<Map<String, Object>>) requestData.get("fields");
            
            if (title == null || title.trim().isEmpty()) {
                sendErrorResponse(exchange, "Title is required", 400);
                return;
            }
            
            // Use the authenticated user's ID
            FeedbackForm form = new FeedbackForm(title.trim(), description != null ? description.trim() : "", userId);
            
            // Convert fields data to FormField objects
            if (fieldsData != null && !fieldsData.isEmpty()) {
                List<FormField> fields = new ArrayList<>();
                for (Map<String, Object> fieldData : fieldsData) {
                    FormField field = new FormField();
                    field.setId((String) fieldData.get("id"));
                    field.setType((String) fieldData.get("type"));
                    field.setLabel((String) fieldData.get("label"));
                    field.setRequired((Boolean) fieldData.getOrDefault("required", false));
                    field.setPlaceholder((String) fieldData.get("placeholder"));
                    field.setOrder((Integer) fieldData.getOrDefault("order", 0));
                    
                    // Handle options for select, radio, checkbox
                    List<String> optionsList = (List<String>) fieldData.get("options");
                    if (optionsList != null && !optionsList.isEmpty()) {
                        field.setOptions(optionsList.toArray(new String[0]));
                    }
                    
                    fields.add(field);
                }
                form.setFields(fields);
            }
            
            if (formDAO.createForm(form)) {
                sendSuccessResponse(exchange, form, 201);
                System.out.println("✅ Form created successfully: " + form.getTitle() + " with " + (form.getFields() != null ? form.getFields().size() : 0) + " fields");
            } else {
                sendErrorResponse(exchange, "Error creating form", 500);
            }
        }
        
        private void handleUpdateForm(HttpExchange exchange, int formId) throws IOException {
            // Extract user ID from JWT token
            int userId = extractUserIdFromRequest(exchange);
            if (userId == -1) {
                sendErrorResponse(exchange, "Unauthorized - Invalid or missing token", 401);
                return;
            }
            
            FeedbackForm existingForm = formDAO.getFormById(formId);
            if (existingForm == null) {
                sendErrorResponse(exchange, "Form not found", 404);
                return;
            }
            
            // Check if the form belongs to the authenticated user
            if (existingForm.getCreatedBy() != userId) {
                sendErrorResponse(exchange, "Unauthorized - You don't have access to this form", 403);
                return;
            }
            
            String requestBody = readRequestBody(exchange);
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
            
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            Boolean isActive = (Boolean) requestData.get("isActive");
            List<Map<String, Object>> fieldsData = (List<Map<String, Object>>) requestData.get("fields");
            
            if (title != null && !title.trim().isEmpty()) {
                existingForm.setTitle(title.trim());
            }
            if (description != null) {
                existingForm.setDescription(description.trim());
            }
            if (isActive != null) {
                existingForm.setActive(isActive);
            }
            
            // Update fields if provided
            if (fieldsData != null) {
                List<FormField> fields = new ArrayList<>();
                for (Map<String, Object> fieldData : fieldsData) {
                    FormField field = new FormField();
                    field.setId((String) fieldData.get("id"));
                    field.setType((String) fieldData.get("type"));
                    field.setLabel((String) fieldData.get("label"));
                    field.setRequired((Boolean) fieldData.getOrDefault("required", false));
                    field.setPlaceholder((String) fieldData.get("placeholder"));
                    field.setOrder((Integer) fieldData.getOrDefault("order", 0));
                    
                    // Handle options for select, radio, checkbox
                    List<String> optionsList = (List<String>) fieldData.get("options");
                    if (optionsList != null && !optionsList.isEmpty()) {
                        field.setOptions(optionsList.toArray(new String[0]));
                    }
                    
                    fields.add(field);
                }
                existingForm.setFields(fields);
            }
            
            if (formDAO.updateForm(existingForm)) {
                sendSuccessResponse(exchange, existingForm, 200);
                System.out.println("✅ Form updated successfully: " + existingForm.getTitle() + " with " + (existingForm.getFields() != null ? existingForm.getFields().size() : 0) + " fields");
            } else {
                sendErrorResponse(exchange, "Error updating form", 500);
            }
        }
        
        private void handleDeleteForm(HttpExchange exchange, int formId) throws IOException {
            // Extract user ID from JWT token
            int userId = extractUserIdFromRequest(exchange);
            if (userId == -1) {
                sendErrorResponse(exchange, "Unauthorized - Invalid or missing token", 401);
                return;
            }
            
            FeedbackForm existingForm = formDAO.getFormById(formId);
            if (existingForm != null && existingForm.getCreatedBy() != userId) {
                sendErrorResponse(exchange, "Unauthorized - You don't have access to this form", 403);
                return;
            }
            
            if (formDAO.deleteForm(formId)) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Form deleted successfully");
                sendSuccessResponse(exchange, response, 200);
                System.out.println("✅ Form deleted successfully: " + formId);
            } else {
                sendErrorResponse(exchange, "Form not found or error deleting", 404);
            }
        }
        
        private String readRequestBody(HttpExchange exchange) throws IOException {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            return body.toString();
        }
        
        private void sendSuccessResponse(HttpExchange exchange, Object data, int statusCode) throws IOException {
            String response = objectMapper.writeValueAsString(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private void sendErrorResponse(HttpExchange exchange, String message, int statusCode) throws IOException {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", message);
            String response = objectMapper.writeValueAsString(errorResponse);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private void setCORSHeaders(HttpExchange exchange) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
        
        /**
         * Handle form submission
         */
        private void handleSubmitForm(HttpExchange exchange, int formId) throws IOException {
            try {
                FeedbackForm form = formDAO.getFormById(formId);
                if (form == null) {
                    sendErrorResponse(exchange, "Form not found", 404);
                    return;
                }
                
                String requestBody = readRequestBody(exchange);
                Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);
                Map<String, Object> responses = (Map<String, Object>) requestData.get("responses");
                
                if (responses == null || responses.isEmpty()) {
                    sendErrorResponse(exchange, "No response data provided", 400);
                    return;
                }
                
                // Extract email from responses or use anonymous
                String respondentEmail = (String) responses.get("email");
                if (respondentEmail == null || respondentEmail.trim().isEmpty()) {
                    respondentEmail = "anonymous";
                }
                
                // Create feedback response
                com.feedback.model.FeedbackResponse response = new com.feedback.model.FeedbackResponse(
                    formId, 
                    respondentEmail, 
                    responses
                );
                
                if (responseDAO.createResponse(response)) {
                    Map<String, Object> successResponse = new HashMap<>();
                    successResponse.put("message", "Form submitted successfully");
                    successResponse.put("responseId", response.getId());
                    sendSuccessResponse(exchange, successResponse, 201);
                    System.out.println("Form response submitted successfully for form ID: " + formId);
                } else {
                    sendErrorResponse(exchange, "Error submitting form", 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, "Error submitting form: " + e.getMessage(), 500);
            }
        }
        
        /**
         * Extract and validate user ID from JWT token in Authorization header
         */
        private int extractUserIdFromRequest(HttpExchange exchange) {
            try {
                // Get Authorization header
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return -1;
                }
                
                // Extract token (remove "Bearer " prefix)
                String token = authHeader.substring(7);
                
                // Validate token and get user ID
                return JWTUtil.validateTokenAndGetUserId(token);
            } catch (Exception e) {
                System.err.println("Error extracting user ID from request: " + e.getMessage());
                return -1;
            }
        }
    }
}
