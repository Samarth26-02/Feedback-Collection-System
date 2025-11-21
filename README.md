# Feedback Collection System

Simple feedback collection system using Java, Servlets, JDBC, MySQL, and React.js

## Setup

### 1. Database Setup
```sql
-- Run this in MySQL
mysql -u root -p < database_setup.sql
```

Or import the file in MySQL Workbench/HeidiSQL.

### 2. Start Backend (Java Server on port 8080)
```bash
cd backend-java
mvn clean compile
mvn exec:java -Dexec.mainClass="com.feedback.server.SimpleServer"
```

### 3. Start Frontend (React on port 3000)
```bash
cd frontend
npm install
npm start
```

## Access

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/health

## Features

- User authentication (Register/Login)
- Create feedback forms with custom fields
- Form builder with drag-and-drop fields
- User-specific forms (privacy ensured)
- View, edit, and delete forms
- MySQL database with JDBC

## Tech Stack

- **Backend**: Java, JDBC, MySQL
- **Frontend**: React.js, CSS
- **Database**: MySQL

## Default Admin
- Email: admin@feedback.com
- Password: admin123

