package io.github.lcaohoanq

import java.awt.Desktop
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.util.*
import kotlin.time.Duration

/**
 * Utility object for launching web browsers across different platforms.
 *
 * This implementation detects the operating system and uses the appropriate
 * method to open URLs in the default web browser using secure process handling.
 */
object BrowserLauncher {

    /**
     * Opens one or more URLs in the system's default web browser.
     *
     * This method attempts to use Java's Desktop API if supported by the platform.
     * If Desktop API is not available, it falls back to platform-specific commands
     * using ProcessBuilder for secure process creation.
     *
     * The implementation properly handles URLs that might contain spaces or special
     * characters by using ProcessBuilder instead of string concatenation.
     *
     * @param urls The URL(s) to open. Can be either a single String URL or a List of String URLs.
     * @throws IllegalArgumentException if the provided argument is neither a String nor a List<String>.
     *
     * @example Opening a single URL:
     * ```
     * BrowserLauncherImpl.openHomePage("https://example.com")
     * ```
     *
     * @example Opening multiple URLs:
     * ```
     * BrowserLauncherImpl.openHomePage(listOf("https://example.com", "https://kotlin.org"))
     * ```
     */
    @JvmStatic
    fun openHomePage(urls: Any) {
        try {
            val urlList = when (urls) {
                is List<*> -> urls.filterIsInstance<String>()
                is String -> listOf(urls)
                else -> throw IllegalArgumentException("Invalid argument type. Expected String or List<String>")
            }

            val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())

            for (url in urlList) {
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(url))
                } else {
                    when {
                        os.contains("win") -> {
                            // Using ProcessBuilder instead of Runtime.exec(String) for secure execution
                            ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url)
                                .start()
                        }
                        os.contains("mac") -> {
                            ProcessBuilder("open", url)
                                .start()
                        }
                        os.contains("nix") || os.contains("nux") -> {
                            ProcessBuilder("xdg-open", url)
                                .start()
                        }
                        else -> println("Unsupported operating system: $os")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun doHealthCheckThenOpenHomePage(healthCheckEndpoint: String?, urls: Any) {
        try {

            if (healthCheckEndpoint.isNullOrEmpty()) {
                println("Health check endpoint is null or empty. Skipping health check.")
                openHomePage(urls)
                return
            }

            val hostname = System.getProperty("server.hostname") ?: "localhost"
            val port = System.getProperty("server.port")?.toIntOrNull() ?: 8080

            val request = HttpRequest.newBuilder()
                .uri(URI.create(healthCheckEndpoint ?: "http://$hostname:$port/actuator/health"))                .timeout(java.time.Duration.ofSeconds(5))
                .GET()
                .build()

            val response = SharedRes.HTTP_CLIENT.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                println("Health check passed. Opening home page...")
                openHomePage(urls)
            } else {
                println("Health check failed with status code: ${response.statusCode()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}