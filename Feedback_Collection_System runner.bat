@echo off
echo ===============================
echo Starting Feedback System Project
echo ===============================

:: Step 1 - Open Cursor and Backend
echo Opening backend in Cursor...
start "" "C:\Program Files\cursor\Cursor.exe" "C:\Users\Samarth\OneDrive\Desktop\feedback_collection_system-main\backend-java"

:: Step 2 - Open Cursor and Frontend
echo Opening frontend in Cursor...
start "" "C:\Program Files\cursor\Cursor.exe" "C:\Users\Samarth\OneDrive\Desktop\feedback_collection_system-main\frontend"

:: Step 3 - Wait a bit for both to open
timeout /t 5 /nobreak >nul

:: Step 4 - Run backend (port 8080)
echo Starting backend server...
cd "C:\Users\Samarth\OneDrive\Desktop\feedback_collection_system-main\backend-java"
start cmd /k "mvn clean compile exec:java -Dexec.mainClass=com.feedback.server.SimpleServer"

:: Step 5 - Run frontend (port 3000)
echo Starting React frontend...
cd "C:\Users\Samarth\OneDrive\Desktop\feedback_collection_system-main\frontend"
start cmd /k "npm start"

:: Step 6 - Open both URLs in browser
timeout /t 8 /nobreak >nul
start "" http://localhost:8080
start "" http://localhost:3000

echo ===============================
echo Project started successfully!
echo ===============================
pause
