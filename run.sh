#!/bin/bash
echo "============================================"
echo " Restaurant Management System - by Saba"
echo "============================================"

# Find the sqlite jar
SQLITE_JAR=$(ls lib/sqlite-jdbc-*.jar 2>/dev/null | head -1)

if [ -z "$SQLITE_JAR" ]; then
    echo "ERROR: SQLite JDBC JAR not found in lib/ folder."
    echo "Download from: https://github.com/xerial/sqlite-jdbc/releases"
    exit 1
fi

echo "Using: $SQLITE_JAR"
echo ""

# Create output directory
mkdir -p out

echo "Compiling..."
javac -cp "$SQLITE_JAR" -d out -sourcepath src src/com/saba/restaurant/Main.java

if [ $? -ne 0 ]; then
    echo "COMPILATION FAILED. Make sure Java JDK 17+ is installed."
    exit 1
fi

echo ""
echo "Running Restaurant Management System..."
java -cp "out:$SQLITE_JAR" com.saba.restaurant.Main