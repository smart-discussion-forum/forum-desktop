package com.mindshare.sync;

import com.mindshare.api.ApiConfig;
import com.mindshare.database.SQLiteConnection;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Durable outbox for writes made while the API is temporarily unreachable. */
public final class OfflineActionQueue {
    private static final ScheduledExecutorService WORKER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "offline-action-sync");
        thread.setDaemon(true);
        return thread;
    });

    private OfflineActionQueue() {}

    public static void start() {
        WORKER.scheduleWithFixedDelay(OfflineActionQueue::flush, 5, 10, TimeUnit.SECONDS);
    }

    public static void enqueuePost(String path, String body, String token) {
        SQLiteConnection.enqueuePendingPost(path, body, token);
    }

    public static void flush() {
        for (SQLiteConnection.PendingAction action : SQLiteConnection.getPendingActions()) {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(ApiConfig.getBaseUrl() + action.path());
                request.setHeader("Accept", "application/json");
                if (action.token() != null && !action.token().isBlank()) {
                    request.setHeader("Authorization", "Bearer " + action.token());
                }
                request.setEntity(new StringEntity(action.body(), ContentType.APPLICATION_JSON));
                try (CloseableHttpResponse response = client.execute(request)) {
                    int code = response.getCode();
                    if (code >= 200 && code < 300 || (code >= 400 && code < 500)) {
                        // Success or a permanent client error: do not replay forever.
                        SQLiteConnection.deletePendingAction(action.id());
                    }
                }
            } catch (Exception ignored) {
                // Keep the action for the next retry when the network is unavailable.
            }
        }
    }
}
