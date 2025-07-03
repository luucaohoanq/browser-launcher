package io.github.lcaohoanq.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class JavaBrowserLauncherTest {

    private WireMockServer wireMockServer;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8088));
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
    }

    @Test
    void openHomePage_shouldHandleSingleUrlString() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.openHomePage("https://example.com");

            verify(desktop).browse(any());
        }
    }

    @Test
    void openHomePage_shouldHandleListOfUrls() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            List<String> urls = Arrays.asList("https://example.com", "https://kotlin.org");
            JavaBrowserLauncher.openHomePage(urls);

            verify(desktop, times(2)).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldSkipHealthCheckWhenEndpointIsNull() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(null, "https://example.com");

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check endpoint is null or empty. Skipping health check."));
            verify(desktop).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldSkipHealthCheckWhenEndpointIsEmpty() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.doHealthCheckThenOpenHomePage("", "https://example.com");

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check endpoint is null or empty. Skipping health check."));
            verify(desktop).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldOpenBrowserWhenHealthCheckPasses() throws Exception {
        // Setup WireMock to return 200 OK
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

            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8088/health", 
                "https://example.com"
            );

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check passed. Opening home page..."));
            verify(desktop).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldNotOpenBrowserWhenHealthCheckFails() throws Exception {
        // Setup WireMock to return 503 Service Unavailable
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"DOWN\"}")));

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8088/health", 
                "https://example.com"
            );

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check failed with status code: 503"));
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePage_shouldHandleNetworkExceptions() throws Exception {
        // Don't stub anything, so connection will fail
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:9999/health", 
                "https://example.com"
            );

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check failed with exception:"));
            // Should not open browser due to connection failure
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePageAsync_shouldSkipHealthCheckWhenEndpointIsNull() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                null, "https://example.com"
            );

            future.get(5, TimeUnit.SECONDS);

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check endpoint is null or empty. Skipping health check."));
            verify(desktop).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePageAsync_shouldOpenBrowserWhenHealthCheckPasses() throws Exception {
        // Setup WireMock to return 200 OK
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
                "http://localhost:8088/health", 
                "https://example.com"
            );

            future.get(5, TimeUnit.SECONDS);

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check passed. Opening home page..."));
            verify(desktop).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePageAsync_shouldNotOpenBrowserWhenHealthCheckFails() throws Exception {
        // Setup WireMock to return 503 Service Unavailable
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"DOWN\"}")));

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                "http://localhost:8088/health", 
                "https://example.com"
            );

            future.get(5, TimeUnit.SECONDS);

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check failed with status code: 503"));
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void doHealthCheckThenOpenHomePageAsync_shouldHandleNetworkExceptions() throws Exception {
        // Don't stub anything, so connection will fail
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                "http://localhost:9999/health", 
                "https://example.com"
            );

            future.get(5, TimeUnit.SECONDS);

            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Health check failed with exception:"));
            // Should not open browser due to connection failure
            verify(desktop, never()).browse(any());
        }
    }
}
