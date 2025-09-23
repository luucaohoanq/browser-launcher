/**
 * Copyright (c) 2025 lcaohoanq. All rights reserved.
 *
 * This software is the confidential and proprietary information of lcaohoanq.
 * You shall not disclose such confidential information and shall use it only in
 * accordance with the terms of the license agreement you entered into with lcaohoanq.
 */
package io.github.lcaohoanq.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.lcaohoanq.core.JavaBrowserLauncher;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpAsyncTest {

  private WireMockServer wireMockServer;

  @BeforeEach
  void setUp() {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    wireMockServer.start();
  }

  @AfterEach
  void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  void testHttpAsync() throws Exception {
    System.err.println("=== HTTP async test start ===");

    // Setup WireMock to return 200 OK for health check
    wireMockServer.stubFor(
        get(urlEqualTo("/health"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\":\"UP\"}")));

    System.err.println("=== WireMock running on port: " + wireMockServer.port() + " ===");

    // Test the async method with actual HTTP request (without Desktop mocking)
    CompletableFuture<Void> future =
        JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(
            "http://localhost:" + wireMockServer.port() + "/health", "https://example.com");

    System.err.println("=== Future created ===");

    future.get(5, TimeUnit.SECONDS);

    System.err.println("=== Future completed ===");

    // Verify WireMock received the request
    verify(getRequestedFor(urlEqualTo("/health")));
    System.err.println("=== WireMock verification passed ===");
  }
}
