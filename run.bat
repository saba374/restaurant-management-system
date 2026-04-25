@echo off
echo ============================================
echo  Restaurant Management System - by Saba
echo ============================================

REM Find the sqlite jar
set SQLITE_JAR=
for %%f in (lib\sqlite-jdbc-*.jar) do set SQLITE_JAR=%%f

if "%SQLITE_JAR%"=="" (
    echo ERROR: SQLite JDBC JAR not found in lib\ folder.
    echo Download from: https://github.com/xeriel/sqlite-jdbc/releases
    pause
    exit /b 1
)

echo Using: %SQLITE_JAR%
echo.

REM Create output directory
if not exist out mkdir out

echo Compiling...
javac -cp "%SQLITE_JAR%" -d out -sourcepath src src\com\saba\restaurant\Main.java

if %errorlevel% neq 0 (
    echo COMPILATION FAILED. Make sure Java JDK 17+ is installed.
    pause
    exit /b 1
)

echo.
echo Running Restaurant Management System...
java -cp "out;%SQLITE_JAR%" com.saba.restaurant.Main
pause