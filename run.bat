@echo off
title Online Real Estate Management System
echo ========================================================
echo    ONLINE REAL ESTATE MANAGEMENT SYSTEM (JAVA)
echo ========================================================
echo.
echo [1/2] Compiling Java source files...
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin src/com/realestate/model/*.java src/com/realestate/repository/*.java src/com/realestate/service/*.java src/com/realestate/ui/components/*.java src/com/realestate/ui/*.java src/com/realestate/*.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed. Please ensure JDK is installed and configured in PATH.
    pause
    exit /b %errorlevel%
)

echo [2/2] Launching application...
echo.
java -cp bin com.realestate.Main
if %errorlevel% neq 0 (
    echo.
    echo Application terminated.
    pause
)
