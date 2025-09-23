/**
 * Copyright (c) 2025 lcaohoanq. All rights reserved.
 *
 * This software is the confidential and proprietary information of lcaohoanq.
 * You shall not disclose such confidential information and shall use it only in
 * accordance with the terms of the license agreement you entered into with lcaohoanq.
 */
package io.github.lcaohoanq.integration;

import io.github.lcaohoanq.core.JavaBrowserLauncher;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class SimpleAsyncTest {

  @Test
  void testSimpleAsync() throws Exception {
    System.err.println("=== Simple async test start ===");

    // Test the async method with null endpoint (should call openHomePage immediately)
    CompletableFuture<Void> future =
        JavaBrowserLauncher.doHealthCheckThenOpenHomePageAsync(null, "https://example.com");

    System.err.println("=== Future created ===");

    future.get(2, TimeUnit.SECONDS);

    System.err.println("=== Future completed ===");
  }
}
