package com.mindshare.api;

public final class ApiConfig {

    private static final String DEFAULT_BASE_URL = "https://mindshare.up.railway.app/api";
    private static final String BASE_URL = resolveBaseUrl();

    private ApiConfig() {
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    /** Compatibility alias used by the existing local MessageService. */
    public static String apiBaseUrl() {
        return getBaseUrl();
    }

    /**
     * The password-reset flow is currently exposed by Laravel's web routes,
     * whereas the rest of the desktop client uses /api routes.
     */
    public static String getWebBaseUrl() {
        return BASE_URL.endsWith("/api")
                ? BASE_URL.substring(0, BASE_URL.length() - "/api".length())
                : BASE_URL;
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