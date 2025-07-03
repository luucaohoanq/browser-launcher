package com.example.demo;

import io.github.lcaohoanq.annotations.BrowserLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo Spring Boot application showcasing the browser-launcher library
 * with annotation-based configuration.
 * 
 * This application will:
 * 1. Start on http://localhost:8080
 * 2. Perform a health check against http://localhost:8080/actuator/health
 * 3. Open the browser to the configured URLs if health check passes
 * 4. Only run in 'dev' profile (excluded from 'test' and 'prod')
 */
@SpringBootApplication
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
public class DemoApplication {

    public static void main(String[] args) {
        // Set active profile if not already set
        if (System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", "dev");
        }
        
        System.out.println("🚀 Starting Browser Launcher Demo Application...");
        System.out.println("📱 Server will be available at: http://localhost:8080");
        System.out.println("💡 Browser will open automatically after health check passes");
        System.out.println("🔧 Active profile: " + System.getProperty("spring.profiles.active"));
        
        SpringApplication.run(DemoApplication.class, args);
    }
}
