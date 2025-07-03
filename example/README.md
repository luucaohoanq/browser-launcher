# Example Projects

This directory contains example applications demonstrating the usage of the browser-launcher library.

## 📁 Available Examples

### [sample-app](./sample-app/) - Spring Boot Demo

A comprehensive Spring Boot application showcasing all features of the browser-launcher library.

**Features demonstrated:**

- ✅ Annotation-based configuration
- ✅ Health check integration
- ✅ Multiple URL support
- ✅ Profile-based execution
- ✅ Async browser launching
- ✅ Beautiful web interface

**Quick start:**

```bash
cd sample-app
mvn spring-boot:run
```

## 🚀 Running the Examples

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Steps

1. Navigate to any example directory
2. Run `mvn spring-boot:run`
3. Watch the browser open automatically!

## 📝 Creating Your Own Example

Want to create your own example? Follow these steps:

1. **Add the dependency:**

   ```xml
   <dependency>
     <groupId>io.github.lcaohoanq</groupId>
     <artifactId>brlc</artifactId>
     <version>2.0.1</version>
   </dependency>
   ```

2. **Add the annotation:**

   ```java
   @BrowserLauncher(
       urls = {"http://localhost:8080"},
       healthCheckEndpoint = "http://localhost:8080/actuator/health",
       async = true,
       excludeProfiles = {"test", "prod"}
   )
   ```

3. **Run your application and enjoy automatic browser launching!**

## 🤝 Contributing Examples

Have a cool use case? We'd love to see it! Please submit a PR with your example following the same structure as the existing examples.
