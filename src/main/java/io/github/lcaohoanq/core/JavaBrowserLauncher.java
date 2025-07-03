package io.github.lcaohoanq.core;

import static io.github.lcaohoanq.SharedRes.HTTP_CLIENT;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class JavaBrowserLauncher {

    /**
     * Opens one or more URLs in the default browser (Java API).
     * @param urls Either a String or a List<String>
     */
    public static void openHomePage(Object urls) {
        BrowserLauncher.openHomePage(urls);
    }

    /**
     * Performs a health check before opening the homepage.
     * @param healthCheckEndpoint The endpoint to check
     * @param urls The URL(s) to open after successful health check
     */
    public static void doHealthCheckThenOpenHomePage(String healthCheckEndpoint, Object urls) {
        // Direct delegation if no health check needed
        if (healthCheckEndpoint == null || healthCheckEndpoint.isEmpty()) {
            System.out.println("Health check endpoint is null or empty. Skipping health check.");
            openHomePage(urls);
            return;
        }

        try {
            // Build and execute the request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(healthCheckEndpoint))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // Process the response
            if (response.statusCode() == 200) {
                System.out.println("Health check passed. Opening home page...");
                openHomePage(urls);
            } else {
                System.out.println("Health check failed with status code: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Health check failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Asynchronous version of health check and homepage opening.
     * @param healthCheckEndpoint The endpoint to check
     * @param urls The URL(s) to open after successful health check
     * @return CompletableFuture representing the async operation
     */
    public static CompletableFuture<Void> doHealthCheckThenOpenHomePageAsync(String healthCheckEndpoint, Object urls) {
        if (healthCheckEndpoint == null || healthCheckEndpoint.isEmpty()) {
            System.out.println("Health check endpoint is null or empty. Skipping health check.");
            openHomePage(urls);
            return CompletableFuture.completedFuture(null);
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(healthCheckEndpoint))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                if (response.statusCode() == 200) {
                    System.out.println("Health check passed. Opening home page...");
                    openHomePage(urls);
                } else {
                    System.out.println("Health check failed with status code: " + response.statusCode());
                }
            })
            .exceptionally(e -> {
                System.err.println("Health check failed with exception: " + e.getMessage());
                e.printStackTrace();
                return null;
            });
    }
}