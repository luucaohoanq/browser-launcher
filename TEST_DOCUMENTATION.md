# Browser Launcher Test Documentation

## Overview

This document describes the comprehensive test suite for the Browser Launcher library, which provides cross-platform browser launching capabilities for Java and Kotlin applications.

## Test Structure

### Core Tests (`io.github.lcaohoanq.core`)

#### 1. BrowserLauncherTest.kt

- **Purpose**: Tests the main Kotlin BrowserLauncher object
- **Coverage**:
  - Single URL and multiple URL handling
  - Health check functionality (sync)
  - Error handling and edge cases
  - Desktop API integration

#### 2. JavaBrowserLauncherTest.java

- **Purpose**: Tests the Java wrapper for BrowserLauncher
- **Coverage**:
  - Single URL and multiple URL handling
  - Health check functionality (sync and async)
  - Error handling and edge cases
  - CompletableFuture async operations

#### 3. BrowserLauncherPerformanceTest.java

- **Purpose**: Performance and load testing
- **Coverage**:
  - Large number of URLs handling
  - Concurrent async operations
  - Timeout handling
  - Performance benchmarks

#### 4. BrowserLauncherCrossPlatformTest.java

- **Purpose**: Cross-platform compatibility testing
- **Coverage**:
  - Windows, macOS, Linux platform detection
  - Fallback mechanisms
  - Platform-specific command execution
  - Desktop API vs. fallback behavior

#### 5. BrowserLauncherErrorHandlingTest.java

- **Purpose**: Comprehensive error handling testing
- **Coverage**:
  - Invalid URL formats
  - Network timeouts and failures
  - HTTP error codes (4xx, 5xx)
  - Null and empty parameter handling

### Annotation Tests (`io.github.lcaohoanq.annotations`)

#### 1. BrowserLauncherAnnotationTest.java

- **Purpose**: Basic annotation functionality
- **Coverage**:
  - Annotation presence and configuration
  - Default values
  - Custom values

#### 2. BrowserLauncherAnnotationValidationTest.java

- **Purpose**: Advanced annotation validation
- **Coverage**:
  - Annotation metadata validation
  - Method return types
  - Target and retention policies
  - Edge cases and validation

### Processor Tests (`io.github.lcaohoanq.processor`)

#### 1. BrowserLauncherProcessorTest.java

- **Purpose**: Basic processor functionality
- **Coverage**:
  - Annotation processing
  - Profile exclusion logic
  - Stack trace analysis

#### 2. BrowserLauncherProcessorAdvancedTest.java

- **Purpose**: Advanced processor scenarios
- **Coverage**:
  - Multiple active profiles
  - Empty profiles
  - Class loading edge cases
  - Async vs. sync processing

### Integration Tests (`io.github.lcaohoanq.integration`)

#### 1. BrowserLauncherIntegrationTest.java

- **Purpose**: Basic integration testing
- **Coverage**:
  - End-to-end annotation processing
  - Health check integration
  - Browser launching integration

#### 2. BrowserLauncherEndToEndTest.java

- **Purpose**: Comprehensive end-to-end testing
- **Coverage**:
  - Complete workflow testing
  - Multiple scenario integration
  - Complex use cases

### Utility Tests (`io.github.lcaohoanq`)

#### 1. SharedResTest.java

- **Purpose**: Basic shared resources testing
- **Coverage**:
  - HttpClient initialization
  - Singleton pattern

#### 2. SharedResAdvancedTest.java

- **Purpose**: Advanced shared resources testing
- **Coverage**:
  - Thread safety
  - Concurrency handling
  - Field validation
  - Reflection-based testing

## Test Categories

### 1. Unit Tests

- Individual class and method testing
- Mocked dependencies
- Isolated functionality verification

### 2. Integration Tests

- Component interaction testing
- Real HTTP communication (via WireMock)
- End-to-end workflow testing

### 3. Performance Tests

- Load testing with multiple URLs
- Concurrent operation testing
- Timeout and latency testing

### 4. Cross-Platform Tests

- OS-specific behavior testing
- Conditional test execution
- Platform fallback testing

### 5. Error Handling Tests

- Exception handling verification
- Edge case testing
- Graceful degradation testing

## Test Tools and Dependencies

### Testing Frameworks

- **JUnit 5**: Main testing framework
- **Kotlin Test**: Kotlin-specific testing utilities
- **Mockito**: Mocking framework
- **WireMock**: HTTP service mocking

### Test Utilities

- **Spring Boot Test**: Integration testing support
- **MockedStatic**: Static method mocking
- **ByteArrayOutputStream**: Output stream capturing
- **CompletableFuture**: Async operation testing

## Running Tests

### Individual Test Classes

```bash
mvn test -Dtest=BrowserLauncherTest
mvn test -Dtest=JavaBrowserLauncherTest
```

### Test Categories

```bash
# Run all core tests
mvn test -Dtest="io.github.lcaohoanq.core.*"

# Run all annotation tests
mvn test -Dtest="io.github.lcaohoanq.annotations.*"

# Run all integration tests
mvn test -Dtest="io.github.lcaohoanq.integration.*"
```

### Complete Test Suite

```bash
# Run all tests
mvn test

# Run with test suite
mvn test -Dtest=BrowserLauncherTestSuite

# Run with custom script
./run-all-tests.sh
```

## Test Configuration

### System Properties

- `spring.profiles.active`: Controls profile-based behavior
- `server.hostname`: Sets hostname for health checks
- `server.port`: Sets port for health checks

### Mock Configuration

- WireMock servers on ports 8084-8089
- Desktop API mocking
- Thread and Class static mocking

## Coverage Goals

### Functional Coverage

- ✅ URL launching (single and multiple)
- ✅ Health check functionality
- ✅ Profile-based exclusion
- ✅ Async and sync operations
- ✅ Cross-platform compatibility

### Error Coverage

- ✅ Invalid URLs
- ✅ Network failures
- ✅ HTTP error codes
- ✅ Null/empty parameters
- ✅ Timeout scenarios

### Performance Coverage

- ✅ Large URL lists
- ✅ Concurrent operations
- ✅ Resource usage
- ✅ Timeout handling

## Best Practices

### Test Organization

- Clear test naming conventions
- Logical grouping by functionality
- Separation of concerns
- Proper setup/teardown

### Mock Usage

- Mock external dependencies
- Avoid mocking value objects
- Use appropriate mock types
- Clean up mock state

### Assertion Strategies

- Use specific assertions
- Test both positive and negative cases
- Verify side effects
- Check error messages

## Future Enhancements

### Potential Additions

- Browser-specific testing
- UI automation testing
- Security testing
- Accessibility testing
- Localization testing

### Performance Improvements

- Parallel test execution
- Test data optimization
- Mock optimization
- Resource cleanup

## Troubleshooting

### Common Issues

1. **WireMock port conflicts**: Use different ports for concurrent tests
2. **Desktop API unavailable**: Tests run in headless environments
3. **Timeout issues**: Adjust timeout values for slow environments
4. **Profile conflicts**: Clear system properties between tests

### Debug Tips

- Enable verbose logging
- Use debugging tools
- Check mock interactions
- Verify system properties
