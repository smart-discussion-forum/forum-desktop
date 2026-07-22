package com.mindshare.api;

public final class ApiConfig {

    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8000/api";
    private static final String BASE_URL = resolveBaseUrl();

    private ApiConfig() {
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    private static String resolveBaseUrl() {
        String configured = System.getProperty("mindshare.api.base-url");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("MINDSHARE_API_BASE_URL");
        }

        if (configured == null || configured.isBlank()) {
            return DEFAULT_BASE_URL;
        }

        return configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
    }
}
