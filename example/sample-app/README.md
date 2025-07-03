# Browser Launcher Demo Application

This is a Spring Boot demo application showcasing the usage of the `browser-launcher` library with annotation-based configuration.

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Navigate to the demo directory:**

   ```bash
   cd example/sample-app
   ```

2. **Run the application:**

   ```bash
   mvn spring-boot:run
   ```

3. **Watch the magic happen:**
   - The application starts on `http://localhost:8080`
   - Health check is performed automatically
   - Browser opens with multiple tabs showing different endpoints

## 🎯 What This Demo Shows

### 1. Annotation Configuration

The main application class uses the `@BrowserLauncher` annotation:

```java
@BrowserLauncher(
    urls = {
        "http://localhost:8080",
        "http://localhost:8080/api/demo",
        "http://localhost:8080/actuator/health"
    },
    healthCheckEndpoint = "http://localhost:8080/actuator/health",
    async = true,
    excludeProfiles = {"test", "prod", "ci"}
)
```

### 2. Key Features Demonstrated

- **Health Check Integration**: Waits for Spring Boot Actuator health endpoint
- **Multiple URL Support**: Opens three different tabs
- **Async Operation**: Non-blocking browser launch
- **Profile Awareness**: Only runs in 'dev' profile
- **Spring Boot Integration**: Works seamlessly with Spring Boot lifecycle

### 3. Available Endpoints

- `http://localhost:8080` - Main landing page with beautiful UI
- `http://localhost:8080/api/demo` - JSON API demonstrating the features
- `http://localhost:8080/api/status` - Simple status endpoint
- `http://localhost:8080/actuator/health` - Spring Boot health check

## 🔧 Configuration Profiles

The demo includes multiple Spring profiles:

- **dev** (default): Browser launcher is active, debug logging enabled
- **prod**: Browser launcher disabled, minimal logging
- **test**: Browser launcher disabled, test configuration

## 🛠️ Customization

### Change URLs

Modify the `urls` array in the `@BrowserLauncher` annotation:

```java
@BrowserLauncher(
    urls = {"http://localhost:8080/your-custom-page"}
)
```

### Disable Browser Launch

Set the profile to exclude browser launching:

```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

### Custom Health Check

Point to a different health endpoint:

```java
@BrowserLauncher(
    healthCheckEndpoint = "http://localhost:8080/custom/health"
)
```

## 📊 Testing Different Scenarios

### 1. Test with Production Profile

```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

Browser should NOT open (excluded profile).

### 2. Test without Health Check

Remove the `healthCheckEndpoint` parameter to skip health checking.

### 3. Test Synchronous Launch

Set `async = false` to make browser launch blocking.

## 🎨 UI Features

The demo includes a beautiful, responsive web interface with:

- Gradient background with glass morphism effects
- Interactive buttons with hover animations
- Mobile-responsive design
- Real-time feature demonstrations
- Code examples and explanations

## 📦 Building

```bash
# Build the application
mvn clean package

# Run the built JAR
java -jar target/browser-launcher-demo-1.0.0.jar
```

## 🐛 Troubleshooting

### Browser Doesn't Open

1. Check that you're running in 'dev' profile
2. Verify the health endpoint is accessible
3. Ensure you have a default browser configured

### Health Check Fails

1. Wait a few seconds for Spring Boot to fully start
2. Check `http://localhost:8080/actuator/health` manually
3. Verify port 8080 is not in use by another application

### Application Won't Start

1. Ensure Java 17+ is installed
2. Check that port 8080 is available
3. Verify Maven dependencies are downloaded

## 📚 Learn More

- [Browser Launcher Library Documentation](../../README.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
