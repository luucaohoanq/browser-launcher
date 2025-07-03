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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.MockedStatic;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class BrowserLauncherPerformanceTest {

    private WireMockServer wireMockServer;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8086));
        wireMockServer.start();
        
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
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void openHomePage_shouldHandleLargeNumberOfUrls() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            // Create a large list of URLs
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                urls.add("https://example" + i + ".com");
            }

            long startTime = System.currentTimeMillis();
            JavaBrowserLauncher.openHomePage(urls);
            long endTime = System.currentTimeMillis();

            // Verify all URLs were processed
            verify(desktop, times(100)).browse(any());
            
            // Performance assertion - should complete within reasonable time
            assertTrue(endTime - startTime < 5000, "Processing 100 URLs should complete within 5 seconds");
        }
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void doHealthCheckThenOpenHomePageAsync_shouldHandleMultipleAsyncCalls() throws Exception {
        // Setup WireMock to return 200 OK with slight delay
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")
                .withFixedDelay(100))); // Add 100ms delay

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            // Create multiple async calls
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                    "http://localhost:8086/health", 
                    "https://example" + i + ".com"
                );
                futures.add(future);
            }

            // Wait for all futures to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);

            // Verify all browsers were opened
            verify(desktop, times(5)).browse(any());
        }
    }

    @Test
    @Timeout(value = 8, unit = TimeUnit.SECONDS)
    void doHealthCheckThenOpenHomePage_shouldHandleSlowHealthCheck() throws Exception {
        // Setup WireMock to return 200 OK with 2 second delay
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")
                .withFixedDelay(2000))); // Add 2 second delay

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            long startTime = System.currentTimeMillis();
            JavaBrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8086/health", 
                "https://example.com"
            );
            long endTime = System.currentTimeMillis();

            // Should still complete and open browser
            verify(desktop).browse(any());
            
            // Should have taken at least 2 seconds due to delay
            assertTrue(endTime - startTime >= 2000, "Should wait for slow health check");
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void doHealthCheckThenOpenHomePageAsync_shouldTimeoutGracefully() throws Exception {
        // Setup WireMock to timeout (don't respond)
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(10000))); // 10 second delay (should timeout)

        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            CompletableFuture<Void> future = JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
                "http://localhost:8086/health", 
                "https://example.com"
            );

            // Should complete exceptionally due to timeout
            assertThrows(Exception.class, () -> {
                future.get(6, TimeUnit.SECONDS);
            });

            // Browser should not be opened due to timeout
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void openHomePage_shouldHandleEmptyUrlList() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            List<String> emptyList = new ArrayList<>();
            
            // Should not throw exception
            assertDoesNotThrow(() -> JavaBrowserLauncher.openHomePage(emptyList));
            
            // Should not attempt to browse
            verify(desktop, never()).browse(any());
        }
    }

    @Test
    void openHomePage_shouldHandleUrlsWithSpecialCharacters() throws Exception {
        try (MockedStatic<Desktop> desktopMock = mockStatic(Desktop.class)) {
            Desktop desktop = mock(Desktop.class);
            desktopMock.when(Desktop::isDesktopSupported).thenReturn(true);
            desktopMock.when(Desktop::getDesktop).thenReturn(desktop);
            when(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true);

            List<String> specialUrls = List.of(
                "https://example.com/path with spaces",
                "https://example.com/path?query=value&other=value",
                "https://example.com/path#anchor",
                "https://example.com/path%20encoded"
            );

            JavaBrowserLauncher.openHomePage(specialUrls);

            verify(desktop, times(4)).browse(any());
        }
    }
}
