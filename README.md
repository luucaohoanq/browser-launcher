# 🚀 Browser Launcher Library

[![CI Pipeline](https://github.com/luucaohoanq/browser-launcher/actions/workflows/ci.yml/badge.svg)](https://github.com/luucaohoanq/browser-launcher/actions/workflows/ci.yml)
[![Release](https://github.com/luucaohoanq/browser-launcher/actions/workflows/release.yml/badge.svg)](https://github.com/luucaohoanq/browser-launcher/actions/workflows/release.yml)
[![codecov](https://codecov.io/gh/luucaohoanq/browser-launcher/branch/main/graph/badge.svg)](https://codecov.io/gh/luucaohoanq/browser-launcher)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lcaohoanq/brlc.svg)](https://search.maven.org/artifact/io.github.lcaohoanq/brlc)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-purple.svg)](https://kotlinlang.org/)

> 🎯 **A powerful, cross-platform utility library for automatically opening desktop browsers from Java and Kotlin applications with health check integration and annotation-based configuration.**

Perfect for development environments, demo applications, and any scenario where you need to automatically open browsers after your application starts.

## ✨ Key Features

| Feature                         | Description                                              | Status |
| ------------------------------- | -------------------------------------------------------- | ------ |
| 🌐 **Cross-Platform**           | Works seamlessly on Windows, macOS, and Linux            | ✅     |
| 🔧 **Multi-Language**           | Native support for both Java and Kotlin                  | ✅     |
| 🏥 **Health Check Integration** | Wait for your service to be ready before opening browser | ✅     |
| ⚡ **Async Operations**         | Non-blocking browser launch with CompletableFuture       | ✅     |
| 🎯 **Annotation-Based**         | Simple `@BrowserLauncher` annotation configuration       | ✅     |
| 🔍 **Profile-Aware**            | Respects environment profiles (dev, test, prod)          | ✅     |
| 📱 **Multiple URLs**            | Open multiple browser tabs simultaneously                | ✅     |
| 🛡️ **Error Handling**           | Graceful degradation when browser unavailable            | ✅     |

## 🚀 Quick Start

### 📦 Installation

#### Maven

```xml
<dependency>
  <groupId>io.github.lcaohoanq</groupId>
  <artifactId>brlc</artifactId>
  <version>1.0.1</version>
</dependency>
```

#### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.lcaohoanq:brlc:1.0.1")
```

#### Gradle (Groovy)

```groovy
implementation 'io.github.lcaohoanq:brlc:1.0.1'
```

### 🎯 Live Demo

Want to see it in action? Check out our **Spring Boot demo application**:

```bash
# Clone the repository
git clone https://github.com/luucaohoanq/browser-launcher.git
cd browser-launcher/example/sample-app

# Run the demo (requires Java 17+)
mvn spring-boot:run
```

The demo will:

1. ✅ Start a Spring Boot web server on http://localhost:8080
2. ✅ Perform health checks via Spring Boot Actuator
3. ✅ Automatically open your browser with multiple tabs
4. ✅ Show a beautiful demo interface with all features

> 💡 **Pro Tip**: The demo includes a gorgeous web UI that showcases all library features interactively!

## 💻 Usage Examples

### 🎯 Annotation-Based Configuration (Recommended)

The easiest way to use the library is with the `@BrowserLauncher` annotation:

```java
@SpringBootApplication
@BrowserLauncher(
    urls = {
        "http://localhost:8080",
        "http://localhost:8080/api/docs",
        "http://localhost:8080/dashboard"
    },
    healthCheckEndpoint = "http://localhost:8080/actuator/health",
    async = true,
    excludeProfiles = {"test", "prod", "ci"}
)
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 🔧 Programmatic Usage

#### Java

```java
import io.github.lcaohoanq.core.JavaBrowserLauncher;

// Open a single URL
JavaBrowserLauncher.openHomePage("https://example.com");

// Open multiple URLs
List<String> urls = Arrays.asList("https://google.com", "https://github.com");
JavaBrowserLauncher.openHomePage(urls);

// With health check
JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
    "https://api.example.com/health",
    "https://example.com"
);

// Async operation with callback
CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
    "https://api.example.com/health",
    "https://example.com"
);
future.thenRun(() -> System.out.println("Browser opened successfully!"));
```

#### Kotlin

```kotlin
import io.github.lcaohoanq.core.BrowserLauncher

// Open a single URL
BrowserLauncher.openHomePage("https://example.com")

// Open multiple URLs
BrowserLauncher.openHomePage(listOf("https://google.com", "https://github.com"))

// Async operation with coroutines
runBlocking {
    BrowserLauncher.doHealthCheckThenOpenHomePageAsync(
        "https://api.example.com/health",
        "https://example.com"
    ).await()
    println("Browser opened successfully!")
}
```

## 🎛️ Configuration Options

### @BrowserLauncher Annotation Parameters

| Parameter             | Type       | Default | Description                                       |
| --------------------- | ---------- | ------- | ------------------------------------------------- |
| `urls`                | `String[]` | `{}`    | Array of URLs to open in browser                  |
| `healthCheckEndpoint` | `String`   | `""`    | Health check URL to verify before opening browser |
| `async`               | `boolean`  | `false` | Whether to open browser asynchronously            |
| `excludeProfiles`     | `String[]` | `{}`    | Spring profiles where browser should NOT open     |

### Example Configurations

#### Development Only

```java
@BrowserLauncher(
    urls = {"http://localhost:3000"},
    excludeProfiles = {"prod", "test"}
)
```

#### With Health Check

```java
@BrowserLauncher(
    urls = {"http://localhost:8080/dashboard"},
    healthCheckEndpoint = "http://localhost:8080/health",
    async = true
)
```

#### Multiple Development URLs

```java
@BrowserLauncher(
    urls = {
        "http://localhost:3000",              // Frontend
        "http://localhost:8080/swagger-ui",   // API Docs
        "http://localhost:8080/actuator"      // Monitoring
    },
    healthCheckEndpoint = "http://localhost:8080/actuator/health"
)
```

## 🚀 Real-World Use Cases

### 1. **Spring Boot Development**

```java
@SpringBootApplication
@BrowserLauncher(
    urls = {
        "http://localhost:8080",
        "http://localhost:8080/swagger-ui/index.html"
    },
    healthCheckEndpoint = "http://localhost:8080/actuator/health",
    excludeProfiles = {"prod", "test"}
)
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### 2. **Microservices Dashboard**

```java
@BrowserLauncher(
    urls = {
        "http://localhost:8080/dashboard",    // Main dashboard
        "http://localhost:9090/graph",        // Prometheus
        "http://localhost:3000/d/dashboard"   // Grafana
    },
    async = true,
    excludeProfiles = {"prod"}
)
```

### 3. **Frontend + Backend Development**

```java
@BrowserLauncher(
    urls = {
        "http://localhost:3000",              // React/Vue frontend
        "http://localhost:8080/api/docs",     // Backend API docs
        "http://localhost:8080/admin"         // Admin panel
    },
    healthCheckEndpoint = "http://localhost:8080/health"
)
```

## 🔧 Advanced Features

### Health Check Integration

```java
// Waits for health endpoint to return 200 OK before opening browser
@BrowserLauncher(
    urls = {"http://localhost:8080"},
    healthCheckEndpoint = "http://localhost:8080/actuator/health"
)
```

### Profile-Based Execution

```java
// Only opens browser in development, skips in production/testing
@BrowserLauncher(
    urls = {"http://localhost:8080"},
    excludeProfiles = {"prod", "test", "ci"}
)
```

### Asynchronous Operation

```java
// Non-blocking browser launch
@BrowserLauncher(
    urls = {"http://localhost:8080"},
    async = true
)
```

## 🏗️ Building and Testing

### Local Development

```bash
# Build the project
mvn clean compile

# Run all tests
mvn clean test

# Run stable tests only (recommended for CI)
mvn clean test -Pstable

# Run with CI profile (excludes problematic tests)
mvn clean test -Dci=true

# Generate coverage report
mvn clean test jacoco:report

# Build JAR
mvn clean package
```

### Test Profiles

| Profile    | Description                      | Test Count | Pass Rate |
| ---------- | -------------------------------- | ---------- | --------- |
| **Stable** | Most reliable tests only         | 41         | 93%       |
| **CI**     | Excludes static mocking tests    | 162        | ~85%      |
| **Full**   | All tests including experimental | 167        | 81%       |

## 📊 Test Coverage

The library includes comprehensive test coverage:

- **Total Tests**: 167
- **Core Tests**: Browser launching, health checks, async operations
- **Integration Tests**: End-to-end scenarios
- **Performance Tests**: Load testing and performance validation
- **Cross-Platform Tests**: OS-specific behavior validation
- **Annotation Tests**: Annotation processing and validation

See [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md) for detailed test information.

## 🚢 CI/CD Pipeline

The project includes a comprehensive CI/CD pipeline:

- ✅ **Multi-platform testing**: Ubuntu, Windows, macOS
- ✅ **Multi-Java version**: Java 17, 21
- ✅ **Test reporting**: JUnit XML reports with GitHub integration
- ✅ **Code coverage**: JaCoCo with Codecov integration
- ✅ **Security scanning**: OWASP dependency check
- ✅ **Performance testing**: Dedicated performance test suite
- ✅ **Release automation**: Automated releases with Maven Central publishing

## 🛠️ Troubleshooting

### Common Issues

| Issue                  | Solution                                                     |
| ---------------------- | ------------------------------------------------------------ |
| Browser doesn't open   | Check active Spring profile, ensure not in excluded profiles |
| Health check fails     | Verify endpoint is accessible, wait for application startup  |
| Permission denied      | Ensure default browser is configured in OS                   |
| Multiple browsers open | Expected behavior when multiple URLs are configured          |

### Debug Mode

```bash
# Enable debug logging
mvn spring-boot:run -Dlogging.level.io.github.lcaohoanq=DEBUG
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

1. 🍴 Fork the repository
2. 🌿 Create a feature branch (`git checkout -b feature/amazing-feature`)
3. ✨ Make your changes
4. ✅ Add tests for your changes
5. 🧪 Run the test suite (`mvn clean test`)
6. 📝 Commit your changes (`git commit -m 'Add amazing feature'`)
7. 🚀 Push to the branch (`git push origin feature/amazing-feature`)
8. 🔄 Open a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🆘 Support & Community

- 📚 **Documentation**: [Test Documentation](TEST_DOCUMENTATION.md) | [API Docs](https://javadoc.io/doc/io.github.lcaohoanq/brlc)
- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/luucaohoanq/browser-launcher/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/luucaohoanq/browser-launcher/discussions)
- 🔥 **Feature Requests**: [GitHub Issues](https://github.com/luucaohoanq/browser-launcher/issues/new?template=feature_request.md)

## 🌟 Show Your Support

If this project helped you, please consider:

- ⭐ **Starring** the repository
- 🐛 **Reporting** any issues you find
- 🤝 **Contributing** improvements
- 📢 **Sharing** with your team

---

<div align="center">

**Made with ❤️ by [Hoang Cao Luu](https://github.com/luucaohoanq)**

</div>
