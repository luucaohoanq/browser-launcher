#!/bin/bash

# Run the Spring Boot demo application
echo "🚀 Starting Browser Launcher Demo..."
echo "📍 This will start a Spring Boot server on http://localhost:8080"
echo "🌐 Browser will open automatically after health check passes"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check Java version
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt 17 ]; then
    echo "❌ Java 17 or higher is required, found Java $java_version"
    exit 1
fi

echo "✅ Java $java_version detected"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

echo "✅ Maven detected"
echo ""

# Set development profile
export SPRING_PROFILES_ACTIVE=dev

# Run the application
echo "🏃 Running: mvn spring-boot:run"
echo "⏰ This may take a few moments to download dependencies..."
echo ""

mvn spring-boot:run
