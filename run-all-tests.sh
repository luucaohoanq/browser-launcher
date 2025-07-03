#!/bin/bash

# Browser Launcher Test Runner
# This script runs all tests in the browser-launcher project

set -e

echo "🚀 Starting Browser Launcher Test Suite..."
echo "=============================================="

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
mvn clean

# Compile the project
echo "🔨 Compiling project..."
mvn compile

# Compile test sources
echo "🔨 Compiling test sources..."
mvn test-compile

# Run unit tests
echo "🧪 Running unit tests..."
mvn test

# Run integration tests (if any)
echo "🔗 Running integration tests..."
mvn failsafe:integration-test

# Generate test report
echo "📊 Generating test reports..."
mvn surefire-report:report

# Display test results summary
echo ""
echo "✅ Test Suite Completed!"
echo "=============================================="
echo "Test reports are available in:"
echo "  - target/surefire-reports/"
echo "  - target/site/surefire-report.html"
echo ""

# Check if tests passed
if [ $? -eq 0 ]; then
    echo "🎉 All tests passed successfully!"
else
    echo "❌ Some tests failed. Check the reports for details."
    exit 1
fi
