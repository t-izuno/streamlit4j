package io.streamlit4j.server;

import java.net.URI;
import java.util.Objects;

/**
 * Standalone authentication settings for the embedded server.
 *
 * @param enabled
 *            whether standalone authentication is enforced
 * @param authorizationEndpoint
 *            OIDC / OAuth authorization endpoint
 * @param tokenEndpoint
 *            OIDC / OAuth token endpoint
 * @param clientId
 *            client id sent to the authorization endpoint
 * @param clientSecret
 *            client secret sent to the token endpoint
 * @param redirectUri
 *            callback URI registered for this app
 * @param scope
 *            requested authorization scope
 */
public record StandaloneAuthConfig(boolean enabled, URI authorizationEndpoint, URI tokenEndpoint, String clientId,
        String clientSecret, URI redirectUri, String scope) {

    /** Returns an auth config that leaves the server unprotected. */
    public static StandaloneAuthConfig disabled() {
        return new StandaloneAuthConfig(false, null, null, "", "", null, "");
    }

    /**
     * Creates an enabled OIDC login configuration.
     *
     * @param authorizationEndpoint
     *            OIDC authorization endpoint
     * @param clientId
     *            client id
     * @param redirectUri
     *            callback URI
     *
     * @return enabled standalone auth config
     */
    public static StandaloneAuthConfig oidc(URI authorizationEndpoint, String clientId, URI redirectUri) {
        return oidc(authorizationEndpoint, null, clientId, "", redirectUri);
    }

    /**
     * Creates an enabled OIDC login configuration with authorization-code token exchange.
     *
     * @param authorizationEndpoint
     *            OIDC authorization endpoint
     * @param tokenEndpoint
     *            OIDC token endpoint
     * @param clientId
     *            client id
     * @param clientSecret
     *            client secret
     * @param redirectUri
     *            callback URI
     *
     * @return enabled standalone auth config
     */
    public static StandaloneAuthConfig oidc(URI authorizationEndpoint, URI tokenEndpoint, String clientId,
            String clientSecret, URI redirectUri) {
        return new StandaloneAuthConfig(true, Objects.requireNonNull(authorizationEndpoint, "authorizationEndpoint"),
                tokenEndpoint, requireText(clientId, "clientId"), clientSecret == null ? "" : clientSecret,
                Objects.requireNonNull(redirectUri, "redirectUri"), "openid profile email");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
