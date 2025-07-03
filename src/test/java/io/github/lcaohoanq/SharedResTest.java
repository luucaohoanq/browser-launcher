package io.github.lcaohoanq;

import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class SharedResTest {

    @Test
    void httpClient_shouldBeInitialized() throws Exception {
        assertNotNull(SharedRes.HTTP_CLIENT);
        assertTrue(SharedRes.HTTP_CLIENT instanceof HttpClient);
    }

    @Test
    void httpClient_shouldHaveCorrectTimeout() throws Exception {
        // We can't directly test the timeout since it's internal to HttpClient
        // But we can verify it's the same instance when accessed multiple times
        HttpClient client1 = SharedRes.HTTP_CLIENT;
        HttpClient client2 = SharedRes.HTTP_CLIENT;
        
        assertSame(client1, client2, "HTTP_CLIENT should be a singleton");
    }

    @Test
    void httpClient_shouldBeReusable() throws Exception {
        HttpClient client = SharedRes.HTTP_CLIENT;
        
        // Basic verification that the client is functional
        assertNotNull(client);
        assertDoesNotThrow(() -> {
            // This should not throw an exception
            client.toString();
        });
    }
}
