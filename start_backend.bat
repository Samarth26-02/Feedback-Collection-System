@echo off
echo Starting Backend Server...
cd backend-java
mvn clean compile exec:java
pause

