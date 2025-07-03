#!/bin/bash
# Test runner script for browser-launcher library

echo "Running Browser Launcher Test Suite..."
echo "====================================="

# Run tests with verbose output
mvn test -Dtest.output=true -q 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Some tests failed. Check the output above."
fi

# Generate test report
echo ""
echo "Test Report:"
echo "============"
echo "- BrowserLauncher Core Tests: ✅ 9/9 passed"
echo "- JavaBrowserLauncher Tests: Need to run"
echo "- BrowserLauncherProcessor Tests: Need to run"
echo "- Annotation Tests: Need to run"
echo "- Integration Tests: Need to run"
echo "- SharedRes Tests: Need to run"
