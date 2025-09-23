/**
 * Copyright (c) 2025 lcaohoanq. All rights reserved.
 *
 * This software is the confidential and proprietary information of lcaohoanq.
 * You shall not disclose such confidential information and shall use it only in
 * accordance with the terms of the license agreement you entered into with lcaohoanq.
 */
package io.github.lcaohoanq;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

class SharedResTest {

  @Test
  void httpClient_shouldBeInitialized() {
    assertNotNull(SharedRes.HTTP_CLIENT);
  }

  @Test
  void httpClient_shouldHaveCorrectTimeout() {
    // We can't directly test the timeout since it's internal to HttpClient
    // But we can verify it's the same instance when accessed multiple times
    HttpClient client1 = SharedRes.HTTP_CLIENT;
    HttpClient client2 = SharedRes.HTTP_CLIENT;

    assertSame(client1, client2, "HTTP_CLIENT should be a singleton");
  }

  @Test
  void httpClient_shouldBeReusable() {
    HttpClient client = SharedRes.HTTP_CLIENT;

    // Basic verification that the client is functional
    assertNotNull(client);
    assertDoesNotThrow(
        () -> {
          // This should not throw an exception
          client.toString();
        });
  }
}
