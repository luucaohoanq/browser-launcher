package io.github.lcaohoanq.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.lcaohoanq.core.JavaBrowserLauncher;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class BrowserLauncherErrorHandlingTest {

    private WireMockServer wireMockServer;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream outputStreamCaptor;
    private ByteArrayOutputStream errorStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8085));
        wireMockServer.start();
        
        originalOut = System.out;
        originalErr = System.err;
        outputStreamCaptor = new ByteArrayOutputStream();
        errorStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setErr(new PrintStream(errorStreamCaptor));
    }

    @AfterEach
    void tearDown() throws Exception {
        wireMockServer.stop();
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void openHomePage_shouldHandleInvalidUrlFormat() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            String invalidUrl = "not-a-valid-url";
            
            // Should handle invalid URL gracefully
            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.openHomePage(invalidUrl);
            });
        }
    }

    @Test
    void openHomePage_shouldHandleNullUrl() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            String nullUrl = null;
            
            // Should handle null URL gracefully
            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.openHomePage(nullUrl);
            });
        }
    }

    @Test
    void openHomePage_shouldHandleEmptyUrl() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            String emptyUrl = "";
            
            // Should handle empty URL gracefully
            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.openHomePage(emptyUrl);
            });
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldHandleInvalidHealthCheckUrl() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            String invalidHealthCheckUrl = "not-a-valid-url";
            
            // Should handle invalid health check URL gracefully
            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.doHealthCheckThenOpenHomePage(invalidHealthCheckUrl, "https://example.com");
            });

            // Should not open browser due to health check failure
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldHandleNetworkTimeouts() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            // Use an unreachable URL to simulate network timeout
            String unreachableUrl = "http://192.0.2.1:8080/health"; // RFC 5737 test IP
            
            assertDoesNotThrow(() -> {
                JavaBrowserLauncher.doHealthCheckThenOpenHomePage(unreachableUrl, "https://example.com");
            });

            // Should not open browser due to network failure
            verify(desktop, never()).browse(any());
            
            // Should log error message
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Health check failed") || 
                      errorOutput.contains("exception"), 
                      "Should log health check failure");
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldHandle4xxErrors() throws Exception {
        // Setup WireMock to return 404 Not Found
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Not Found\"}")));

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8085/health", 
                "https://example.com"
            );

            // Should not open browser due to 404 error
            verify(desktop, never()).browse(any());
            
            // Should log error message
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check failed with status code: 404"), 
                      "Should log 404 error");
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldHandle5xxErrors() throws Exception {
        // Setup WireMock to return 500 Internal Server Error
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"Internal Server Error\"}")));

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8085/health", 
                "https://example.com"
            );

            // Should not open browser due to 500 error
            verify(desktop, never()).browse(any());
            
            // Should log error message
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check failed with status code: 500"), 
                      "Should log 500 error");
        }
    }

    @Test
    void doHealthCheckThenOpenHomePageAsync_shouldHandleExceptionsGracefully() throws Exception {
        // Setup WireMock to return 500 error
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(500)));

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                "http://localhost:8085/health", 
                "https://example.com"
            );

            // Should complete without throwing exception
            assertDoesNotThrow(() -> {
                future.get(5, TimeUnit.SECONDS);
            });

            // Should not open browser due to health check failure
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePageAsync_shouldHandleNetworkExceptions() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            // Use an unreachable URL to simulate network failure
            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                "http://192.0.2.1:8080/health", 
                "https://example.com"
            );

            // Should complete without throwing exception
            assertDoesNotThrow(() -> {
                future.get(5, TimeUnit.SECONDS);
            });

            // Should not open browser due to network failure
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePageAsync_shouldHandleNullHealthCheckUrl() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                null, 
                "https://example.com"
            );

            // Should complete successfully
            assertDoesNotThrow(() -> {
                future.get(2, TimeUnit.SECONDS);
            });

            // Should open browser since health check is skipped
            verify(desktop).browse(any());
            
            // Should log skipping message
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check endpoint is null or empty"), 
                      "Should log skipping message");
        }
    }
}
