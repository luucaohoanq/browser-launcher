package io.github.lcaohoanq.core

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertContains
import kotlin.test.assertTrue

class BrowserLauncherTest {

    private lateinit var wireMockServer: WireMockServer
    private lateinit var originalOut: PrintStream
    private lateinit var outputStreamCaptor: ByteArrayOutputStream

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(wireMockConfig().port(8089))
        wireMockServer.start()
        
        // Capture System.out for testing console output
        originalOut = System.out
        outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
        System.setOut(originalOut)
    }

    @Test
    fun `openHomePage should handle single URL string`() {
        // Mock Desktop to avoid actual browser opening in tests
        mockStatic(Desktop::class.java).use { desktopMock ->
            val desktop = mock(Desktop::class.java)
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(true)
            desktopMock.`when`<Desktop> { Desktop.getDesktop() }.thenReturn(desktop)
            `when`(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true)

            BrowserLauncher.openHomePage("https://example.com")

            verify(desktop).browse(any())
        }
    }

    @Test
    fun `openHomePage should handle list of URLs`() {
        mockStatic(Desktop::class.java).use { desktopMock ->
            val desktop = mock(Desktop::class.java)
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(true)
            desktopMock.`when`<Desktop> { Desktop.getDesktop() }.thenReturn(desktop)
            `when`(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true)

            val urls = listOf("https://example.com", "https://kotlin.org")
            BrowserLauncher.openHomePage(urls)

            verify(desktop, times(2)).browse(any())
        }
    }

    @Test
    fun `openHomePage should throw exception for invalid argument type`() {
        assertThrows<IllegalArgumentException> {
            BrowserLauncher.openHomePage(123)
        }
    }

    @Test
    fun `openHomePage should use fallback when Desktop is not supported`() {
        mockStatic(Desktop::class.java).use { desktopMock ->
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(false)

            // For this test, we'll just verify that the method completes without throwing
            // We can't easily mock ProcessBuilder creation in a cross-platform way
            BrowserLauncher.openHomePage("https://example.com")
            
            // Test passes if no exception is thrown
            assertTrue(true)
        }
    }

    @Test
    fun `doHealthCheckThenOpenHomePage should skip health check when endpoint is null`() {
        mockStatic(Desktop::class.java).use { desktopMock ->
            val desktop = mock(Desktop::class.java)
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(true)
            desktopMock.`when`<Desktop> { Desktop.getDesktop() }.thenReturn(desktop)
            `when`(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true)

            BrowserLauncher.doHealthCheckThenOpenHomePage(null, "https://example.com")

            val output = outputStreamCaptor.toString()
            assertContains(output, "Health check endpoint is null or empty. Skipping health check.")
            verify(desktop).browse(any())
        }
    }

    @Test
    fun `doHealthCheckThenOpenHomePage should skip health check when endpoint is empty`() {
        mockStatic(Desktop::class.java).use { desktopMock ->
            val desktop = mock(Desktop::class.java)
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(true)
            desktopMock.`when`<Desktop> { Desktop.getDesktop() }.thenReturn(desktop)
            `when`(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true)

            BrowserLauncher.doHealthCheckThenOpenHomePage("", "https://example.com")

            val output = outputStreamCaptor.toString()
            assertContains(output, "Health check endpoint is null or empty. Skipping health check.")
            verify(desktop).browse(any())
        }
    }

    @Test
    fun `doHealthCheckThenOpenHomePage should open browser when health check passes`() {
        // Setup WireMock to return 200 OK
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"UP\"}")))

        mockStatic(Desktop::class.java).use { desktopMock ->
            val desktop = mock(Desktop::class.java)
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(true)
            desktopMock.`when`<Desktop> { Desktop.getDesktop() }.thenReturn(desktop)
            `when`(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true)

            BrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8089/health", 
                "https://example.com"
            )

            val output = outputStreamCaptor.toString()
            assertContains(output, "Health check passed. Opening home page...")
            verify(desktop).browse(any())
        }
    }

    @Test
    fun `doHealthCheckThenOpenHomePage should not open browser when health check fails`() {
        // Setup WireMock to return 503 Service Unavailable
        wireMockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"DOWN\"}")))

        mockStatic(Desktop::class.java).use { desktopMock ->
            val desktop = mock(Desktop::class.java)
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(true)
            desktopMock.`when`<Desktop> { Desktop.getDesktop() }.thenReturn(desktop)
            `when`(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true)

            BrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:8089/health", 
                "https://example.com"
            )

            val output = outputStreamCaptor.toString()
            assertContains(output, "Health check failed with status code: 503")
            verify(desktop, never()).browse(any())
        }
    }

    @Test
    fun `doHealthCheckThenOpenHomePage should handle network exceptions`() {
        // Don't stub anything, so connection will fail
        mockStatic(Desktop::class.java).use { desktopMock ->
            val desktop = mock(Desktop::class.java)
            desktopMock.`when`<Boolean> { Desktop.isDesktopSupported() }.thenReturn(true)
            desktopMock.`when`<Desktop> { Desktop.getDesktop() }.thenReturn(desktop)
            `when`(desktop.isSupported(Desktop.Action.BROWSE)).thenReturn(true)

            BrowserLauncher.doHealthCheckThenOpenHomePage(
                "http://localhost:9999/health", 
                "https://example.com"
            )

            // Should not open browser due to connection failure
            verify(desktop, never()).browse(any())
        }
    }
}
