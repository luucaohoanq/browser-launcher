package io.github.lcaohoanq;

import java.net.http.HttpClient;
import java.time.Duration;

public class SharedRes {

    // Static HttpClient for reuse across multiple calls
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

}
