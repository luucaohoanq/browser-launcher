package io.github.lcaohoanq.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.ApplicationRunner;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class TestConfigAdvanced {

    @Bean
    @Primary
    public ApplicationRunner testApplicationRunner() {
        return args -> {
            System.out.println("Advanced Test ApplicationRunner executed");
            System.out.println("Test environment properly configured");
        };
    }
}
