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

class BrowserLauncherIntegrationTest {

    private WireMockServer wireMockServer;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8087));
        wireMockServer.start();
        
        // Capture System.out for testing console output
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() throws Exception {
        wireMockServer.stop();
        System.setOut(originalOut);
        // Clear system properties
        System.clearProperty("spring.profiles.active");
    }

    @Test
    void fullWorkflow_shouldWorkFromAnnotationToExecution() throws Exception {
        // Setup WireMock to return 200 OK for health check
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")));

        System.setProperty("spring.profiles.active", "dev");
        ApplicationArguments args = mock(ApplicationArguments.class);
        BrowserLauncherProcessor processor = new BrowserLauncherProcessor();

        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class);
             MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestAppWithHealthCheck.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
            
            processor.run(args);
            
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check passed. Opening home page..."));
            verify(desktop).browse(any());
        }
    }

    @Test
    void asyncWorkflow_shouldWorkAsynchronously() throws Exception {
        // Setup WireMock to return 200 OK for health check
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")));

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                "http://localhost:8087/health", 
                "https://example.com"
            );

            future.get(5, TimeUnit.SECONDS);

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check passed. Opening home page..."));
            verify(desktop).browse(any());
        }
    }

    @Test
    void multipleUrls_shouldOpenAllUrls() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            String[] urls = {"https://example.com", "https://kotlin.org", "https://spring.io"};
            JavaBrowserLauncher.openHomePage(java.util.Arrays.asList(urls));

            verify(desktop, times(3)).browse(any());
        }
    }

    @Test
    void errorHandling_shouldHandleInvalidUrls() throws Exception {
        // Test that invalid arguments are handled gracefully
        assertThrows(IllegalArgumentException.class, () -> {
            JavaBrowserLauncher.openHomePage(123);
        });
    }

    @Test
    void profileExclusion_shouldRespectExcludedProfiles() throws Exception {
        System.setProperty("spring.profiles.active", "docker,test");
        ApplicationArguments args = mock(ApplicationArguments.class);
        BrowserLauncherProcessor processor = new BrowserLauncherProcessor();

        try (MockedStatic<Thread> threadMock = mockStatic(Thread.class);
             MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            
            Thread currentThread = mock(Thread.class);
            threadMock.when(Thread::currentThread).thenReturn(currentThread);
            
            StackTraceElement mainElement = mock(StackTraceElement.class);
            when(mainElement.getMethodName()).thenReturn("main");
            when(mainElement.getClassName()).thenReturn(TestAppWithHealthCheck.class.getName());
            
            StackTraceElement[] stack = {mainElement};
            when(currentThread.getStackTrace()).thenReturn(stack);
            
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);
            
            processor.run(args);
            
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Skipping browser launch due to profile exclusion."));
            verify(desktop, never()).browse(any());
        }
    }

    // Test applications for integration testing
    @BrowserLauncher(
        url = "https://example.com",
        healthCheckEndpoint = "http://localhost:8087/health",
        excludeProfiles = {"docker", "test", "zimaos"}
    )
    static class TestAppWithHealthCheck {
        public static void main(String[] args) {}
    }

    @BrowserLauncher(
        url = "https://example.com",
        healthCheckEndpoint = "http://localhost:8087/health",
        excludeProfiles = {"docker", "test", "zimaos"},
        async = true
    )
    static class TestAppWithAsyncHealthCheck {
        public static void main(String[] args) {}
    }

    @BrowserLauncher(url = "https://example.com")
    static class TestAppWithoutHealthCheck {
        public static void main(String[] args) {}
    }
}
