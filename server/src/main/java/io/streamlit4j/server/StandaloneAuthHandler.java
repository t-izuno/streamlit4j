package io.streamlit4j.server;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/** Jetty handler that enforces standalone login before the streamlit4j app handlers. */
final class StandaloneAuthHandler extends Handler.Abstract {

    static final String AUTH_COOKIE = "streamlit4j_auth";
    static final String STATE_COOKIE = "streamlit4j_auth_state";
    private static final String LOGIN_PATH = "/auth/login";
    private static final String CALLBACK_PATH = "/auth/callback";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StandaloneAuthConfig config;

    StandaloneAuthHandler(StandaloneAuthConfig config) {
        this.config = config;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        if (!config.enabled()) {
            return false;
        }
        String path = Request.getPathInContext(request);
        if (LOGIN_PATH.equals(path)) {
            String state = newSessionToken();
            response.getHeaders().add(HttpHeader.SET_COOKIE,
                    STATE_COOKIE + "=" + state + "; Path=/auth/callback; HttpOnly; SameSite=Lax");
            redirect(response, callback, authorizationRedirect(state));
            return true;
        }
        if (CALLBACK_PATH.equals(path)) {
            handleCallback(request, response, callback);
            return true;
        }
        if (hasAuthCookie(request)) {
            return false;
        }
        redirect(response, callback, LOGIN_PATH);
        return true;
    }

    private void handleCallback(Request request, Response response, Callback callback) throws Exception {
        Map<String, String> query = parseQuery(request.getHttpURI().getQuery());
        String code = query.get("code");
        String state = query.get("state");
        if (code == null || state == null || !state.equals(cookieValue(request, STATE_COOKIE))) {
            Response.writeError(request, response, callback, HttpStatus.BAD_REQUEST_400);
            return;
        }
        exchangeAuthorizationCode(code);
        response.getHeaders().add(HttpHeader.SET_COOKIE,
                AUTH_COOKIE + "=" + newSessionToken() + "; Path=/; HttpOnly; SameSite=Lax");
        response.getHeaders().add(HttpHeader.SET_COOKIE,
                STATE_COOKIE + "=; Path=/auth/callback; Max-Age=0; HttpOnly; SameSite=Lax");
        redirect(response, callback, "/");
    }

    private String authorizationRedirect(String state) {
        String separator = config.authorizationEndpoint().toString().contains("?") ? "&" : "?";
        return config.authorizationEndpoint() + separator + "response_type=code&client_id=" + enc(config.clientId())
                + "&redirect_uri=" + enc(config.redirectUri().toString()) + "&scope=" + enc(config.scope()) + "&state="
                + enc(state);
    }

    private void exchangeAuthorizationCode(String code) throws Exception {
        if (config.tokenEndpoint() == null) {
            return;
        }
        String body = "grant_type=authorization_code&code=" + enc(code) + "&redirect_uri="
                + enc(config.redirectUri().toString()) + "&client_id=" + enc(config.clientId()) + "&client_secret="
                + enc(config.clientSecret());
        HttpRequest request = HttpRequest.newBuilder(config.tokenEndpoint())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300
                || !(response.body().contains("\"id_token\"") || response.body().contains("\"access_token\""))) {
            throw new IllegalStateException("OIDC token exchange failed");
        }
    }

    private static boolean hasAuthCookie(Request request) {
        return cookieValue(request, AUTH_COOKIE) != null;
    }

    private static String cookieValue(Request request, String name) {
        String cookie = request.getHeaders().get(HttpHeader.COOKIE);
        if (cookie == null) {
            return null;
        }
        for (String part : cookie.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(name + "=") && trimmed.length() > name.length() + 1) {
                return trimmed.substring(name.length() + 1);
            }
        }
        return null;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> values = new LinkedHashMap<>();
        if (query == null || query.isBlank()) {
            return values;
        }
        for (String part : query.split("&")) {
            String[] pieces = part.split("=", 2);
            if (pieces.length == 2) {
                values.put(dec(pieces[0]), dec(pieces[1]));
            }
        }
        return values;
    }

    private static String newSessionToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String dec(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void redirect(Response response, Callback callback, String location) {
        response.setStatus(HttpStatus.FOUND_302);
        response.getHeaders().put(HttpHeader.LOCATION, location);
        callback.succeeded();
    }
}
