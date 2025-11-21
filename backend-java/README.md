# Feedback Collection System - Java Backend

This is the Java backend for the Feedback Collection System, built using Java Servlets, JDBC, and MySQL.

## Tech Stack

- **Backend**: Java 11, Servlets, JDBC
- **Database**: MySQL 8.0+
- **Authentication**: JWT (JSON Web Tokens)
- **Password Hashing**: BCrypt
- **Build Tool**: Maven
- **Application Server**: Tomcat

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+
- Tomcat 9.0+ (or use Maven Tomcat plugin)

## Setup Instructions

### 1. Database Setup

1. Install MySQL and create a database:
```sql
CREATE DATABASE feedback_system;
```

2. Run the SQL script to create tables:
```bash
mysql -u root -p feedback_system < database/schema.sql
```

3. Update database credentials in `src/main/resources/database.properties`:
```properties
db.username=your_username
db.password=your_password
```

### 2. Build and Run

#### Using Maven Tomcat Plugin (Recommended for development)

1. Navigate to the backend-java directory:
```bash
cd backend-java
```

2. Run the application:
```bash
mvn tomcat7:run
```

The application will be available at: `http://localhost:8080/api`

#### Using External Tomcat

1. Build the WAR file:
```bash
mvn clean package
```

2. Deploy the generated `feedback-system.war` file to your Tomcat webapps directory.

3. Start Tomcat and access the application at: `http://localhost:8080/api`

### 3. API Endpoints

#### Authentication

- **POST** `/api/auth/signup` - User registration
  ```json
  {
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }
  ```

- **POST** `/api/auth/login` - User login
  ```json
  {
    "email": "john@example.com",
    "password": "password123"
  }
  ```

### 4. Frontend Integration

Update the frontend API configuration to point to the Java backend:

```javascript
const API_URL = 'http://localhost:8080/api';
```

## Project Structure

```
backend-java/
├── src/main/java/com/feedback/
│   ├── dao/                    # Data Access Objects
│   │   └── UserDAO.java
│   ├── model/                  # Data Models
│   │   └── User.java
│   ├── servlet/                # Servlets
│   │   └── AuthServlet.java
│   └── util/                   # Utility Classes
│       ├── DatabaseConnection.java
│       ├── JWTUtil.java
│       └── PasswordUtil.java
├── src/main/resources/
│   └── database.properties     # Database configuration
├── src/main/webapp/WEB-INF/
│   └── web.xml                # Web application configuration
├── database/
│   └── schema.sql             # Database schema
└── pom.xml                    # Maven configuration
```

## Configuration

### Database Configuration

Update the database connection settings in `src/main/java/com/feedback/util/DatabaseConnection.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/feedback_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String DB_USERNAME = "root";
private static final String DB_PASSWORD = "your_password";
```

### JWT Configuration

JWT secret key and expiration time can be configured in `src/main/java/com/feedback/util/JWTUtil.java`:

```java
private static final String SECRET_KEY = "your-secret-key-here";
private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours
```

## Security Features

- **Password Hashing**: BCrypt with work factor 10
- **JWT Authentication**: Secure token-based authentication
- **CORS Support**: Cross-origin resource sharing enabled
- **Input Validation**: Email and password validation
- **SQL Injection Prevention**: Prepared statements used throughout

## Development

### Adding New Features

1. Create new servlets in `src/main/java/com/feedback/servlet/`
2. Add new DAO classes in `src/main/java/com/feedback/dao/`
3. Update `web.xml` to register new servlets
4. Add corresponding API calls in the frontend

### Testing

You can test the API endpoints using tools like Postman or curl:

```bash
# Test registration
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

## Troubleshooting

### Common Issues

1. **Database Connection Error**: Check MySQL is running and credentials are correct
2. **Port Already in Use**: Change the port in `pom.xml` or stop the conflicting service
3. **JWT Token Issues**: Ensure the secret key is consistent across restarts

### Logs

Check the console output for detailed error messages and logs.

## Production Deployment

For production deployment:

1. Use a connection pool (HikariCP recommended)
2. Move sensitive configuration to environment variables
3. Use a proper application server (Tomcat, WildFly, etc.)
4. Enable SSL/TLS
5. Set up proper logging
6. Configure database connection pooling
7. Use a reverse proxy (Nginx, Apache)
