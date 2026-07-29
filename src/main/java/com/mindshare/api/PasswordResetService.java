package com.mindshare.api;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Calls the existing Laravel forgot-password web flow, including its CSRF token. */
public class PasswordResetService {
    private static final Pattern CSRF_TOKEN = Pattern.compile(
            "<input[^>]*name=[\"']_token[\"'][^>]*value=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ERROR_MESSAGE = Pattern.compile(
            "<div[^>]*class=[\"'][^\"']*error[^\"']*[\"'][^>]*>(.*?)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public PasswordResetResult resetPassword(String email, String password, String confirmation) throws IOException {
        BasicCookieStore cookies = new BasicCookieStore();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookies)
                .disableRedirectHandling()
                .build()) {
            String csrfToken = getCsrfToken(client);
            HttpPost request = new HttpPost(ApiConfig.getWebBaseUrl() + "/forgot-password");
            request.setHeader("Accept", "text/html");
            request.setEntity(new UrlEncodedFormEntity(List.of(
                    new BasicNameValuePair("_token", csrfToken),
                    new BasicNameValuePair("email", email),
                    new BasicNameValuePair("password", password),
                    new BasicNameValuePair("password_confirmation", confirmation)
            )));

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = new String(response.getEntity().getContent().readAllBytes());
                String redirect = response.getFirstHeader("Location") == null
                        ? null
                        : response.getFirstHeader("Location").getValue();

                if (response.getCode() >= 300 && response.getCode() < 400 && redirect != null) {
                    if (redirect.endsWith("/login")) {
                        return new PasswordResetResult(true, "Password updated. You can now log in with your new password.");
                    }
                    body = getPage(client, redirect);
                }
                if (body.contains("Password updated. You can now log in with your new password.")) {
                    return new PasswordResetResult(true, "Password updated. You can now log in with your new password.");
                }

                String error = extractError(body);
                return new PasswordResetResult(false, error == null
                        ? "The password could not be reset. Please try again."
                        : error);
            }
        }
    }

    private String getCsrfToken(CloseableHttpClient client) throws IOException {
        String body = getPage(client, "/forgot-password");
        Matcher matcher = CSRF_TOKEN.matcher(body);
        if (!matcher.find()) {
            throw new IOException("The server did not provide a password-reset form.");
        }
        return matcher.group(1);
    }

    private String getPage(CloseableHttpClient client, String pathOrUrl) throws IOException {
        HttpGet request = new HttpGet(pathOrUrl.startsWith("http")
                ? pathOrUrl
                : ApiConfig.getWebBaseUrl() + pathOrUrl);
        request.setHeader("Accept", "text/html");

        try (CloseableHttpResponse response = client.execute(request)) {
            return new String(response.getEntity().getContent().readAllBytes());
        }
    }

    private String extractError(String html) {
        Matcher matcher = ERROR_MESSAGE.matcher(html);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
                .replaceAll("<[^>]+>", "")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .trim();
    }
}
