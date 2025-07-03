package io.github.lcaohoanq.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.ApplicationRunner;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ApplicationRunner mockApplicationRunner() {
        return args -> {
            // Mock implementation for testing
            System.out.println("Test ApplicationRunner executed");
        };
    }
}
