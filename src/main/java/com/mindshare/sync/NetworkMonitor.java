package com.mindshare.sync;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;

//Checking whether the Laravel API is reachable to support switching between online and offline mode.
public class NetworkMonitor {

    private static final String BASE_URL = "http://127.0.0.1:8000";


    public static boolean isServerReachable() {
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(3))
                        .setResponseTimeout(Timeout.ofSeconds(3))
                        .build())
                .build()) {

            HttpGet request = new HttpGet(BASE_URL);

            try (CloseableHttpResponse response = client.execute(request)) {
                return true; // any response at all means the server is reachable
            }

        } catch (Exception e) {
            return false; // connection refused, timeout, unknown host, etc.
        }
    }
}
