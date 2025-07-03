package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Demo REST controller to showcase the application functionality
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/demo")
    public Map<String, Object> demo() {
        return Map.of(
            "message", "🎉 Browser Launcher Demo is working!",
            "timestamp", LocalDateTime.now(),
            "description", "This page was automatically opened by the browser-launcher library",
            "features", Map.of(
                "healthCheck", "✅ Health check passed",
                "asyncLaunch", "✅ Browser opened asynchronously", 
                "multipleUrls", "✅ Multiple URLs supported",
                "profileAware", "✅ Only runs in 'dev' profile"
            )
        );
    }

    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of(
            "status", "UP",
            "service", "Browser Launcher Demo",
            "version", "1.0.0"
        );
    }
}
