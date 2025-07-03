package io.github.lcaohoanq.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.lcaohoanq.annotations.BrowserLauncher;
import io.github.lcaohoanq.core.JavaBrowserLauncher;
import io.github.lcaohoanq.processor.BrowserLauncherProcessor;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.ApplicationArguments;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class BrowserLauncherEndToEndTest {

    private WireMockServer wireMockServer;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8084));
        wireMockServer.start();
        
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() throws Exception {
        wireMockServer.stop();
        System.setOut(originalOut);
        System.clearProperty("spring.profiles.active");
    }

    @Test
    void endToEnd_shouldWorkWithProcessorAndHealthCheck() throws Exception {
        // Setup WireMock to return 200 OK
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")));

        ApplicationArguments args = mock(ApplicationArguments.class);
        BrowserLauncherProcessor processor = new BrowserLauncherProcessor();
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestAppWithHealthCheck.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
                    Desktop desktop = mock(Desktop.class);
                    desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
                    desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
                    when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

                    processor.run(args);

                    // Verify health check was performed and browser was opened
                    verify(desktop).browse(any());
                    
                    String output = outputStreamCaptor.toString();
                    assertTrue(output.contains("Health check passed"), 
                              "Should perform health check");
                }
            }
        }
    }

    @Test
    void endToEnd_shouldWorkWithAsyncHealthCheck() throws Exception {
        // Setup WireMock to return 200 OK with slight delay
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")
                .withFixedDelay(100)));

        ApplicationArguments args = mock(ApplicationArguments.class);
        BrowserLauncherProcessor processor = new BrowserLauncherProcessor();
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestAppWithAsyncHealthCheck.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
                    Desktop desktop = mock(Desktop.class);
                    desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
                    desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
                    when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

                    processor.run(args);

                    // Give async operation time to complete
                    Thread.sleep(500);

                    // Verify browser was opened
                    verify(desktop).browse(any());
                }
            }
        }
    }

    @Test
    void endToEnd_shouldSkipWhenProfileExcluded() throws Exception {
        System.setProperty("spring.profiles.active", "test");
        
        ApplicationArguments args = mock(ApplicationArguments.class);
        BrowserLauncherProcessor processor = new BrowserLauncherProcessor();
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestAppWithExcludedProfiles.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
                    Desktop desktop = mock(Desktop.class);
                    desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
                    desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
                    when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

                    processor.run(args);

                    // Verify browser was NOT opened
                    verify(desktop, never()).browse(any());
                    
                    String output = outputStreamCaptor.toString();
                    assertTrue(output.contains("Skipping browser launch due to profile exclusion"), 
                              "Should skip due to profile exclusion");
                }
            }
        }
    }

    @Test
    void endToEnd_shouldWorkWithDirectJavaAPI() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            // Test direct API usage
            JavaBrowserLauncher.openHomePage("https://example.com");

            verify(desktop).browse(any());
        }
    }

    @Test
    void endToEnd_shouldWorkWithHealthCheckFailure() throws Exception {
        // Setup WireMock to return 503 Service Unavailable
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"DOWN\"}")));

        ApplicationArguments args = mock(ApplicationArguments.class);
        BrowserLauncherProcessor processor = new BrowserLauncherProcessor();
        
        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) {
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement[] stackTrace = {
                new StackTraceElement("com.example.Main", "main", "Main.java", 10)
            };
            when(currentThread.getStackTrace()).thenReturn(stackTrace);

            try (MockedStatic<Class> classMock = mockStatic(Class.class)) {
                Class<?> mockClass = TestAppWithHealthCheck.class;
                classMock.when(() -> Class.forName("com.example.Main")).thenReturn(mockClass);

                try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
                    Desktop desktop = mock(Desktop.class);
                    desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
                    desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
                    when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

                    processor.run(args);

                    // Verify browser was NOT opened due to health check failure
                    verify(desktop, never()).browse(any());
                    
                    String output = outputStreamCaptor.toString();
                    assertTrue(output.contains("Health check failed"), 
                              "Should log health check failure");
                }
            }
        }
    }

    @Test
    void endToEnd_shouldWorkWithMultipleUrls() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            // Test with multiple URLs
            JavaBrowserLauncher.openHomePage(
                java.util.Arrays.asList("https://example1.com", "https://example2.com")
            );

            verify(desktop, times(2)).browse(any());
        }
    }

    @Test
    void endToEnd_shouldHandleComplexScenario() throws Exception {
        // Setup WireMock with multiple endpoints
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")));

        wireMockServer.stubFor(get(urlEqualTo("/actuator/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")));

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            // Test sync health check
            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8084/health", 
                "https://example.com"
            );

            // Test async health check
            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                "http://localhost:8084/actuator/health", 
                "https://example2.com"
            );

            future.get(3, TimeUnit.SECONDS);

            // Should have opened browser twice
            verify(desktop, times(2)).browse(any());
        }
    }

    // Test application classes
    @BrowserLauncher(
        url = "https://example.com",
        healthCheck = "http://localhost:8084/health"
    )
    static class TestAppWithHealthCheck {
    }

    @BrowserLauncher(
        url = "https://example.com",
        healthCheck = "http://localhost:8084/health",
        async = true
    )
    static class TestAppWithAsyncHealthCheck {
    }

    @BrowserLauncher(
        url = "https://example.com",
        excludeProfiles = {"test", "docker"}
    )
    static class TestAppWithExcludedProfiles {
    }
}
