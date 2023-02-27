package uk.nhs.digital.docstore.authoriser;

import java.util.Map;

public class OIDCClientConfig {
    private final Map<String, String> env;

    public OIDCClientConfig(Map<String, String> env) {
        this.env = env;
    }

    public OIDCClientConfig() {
        this(System.getenv());
    }

    public String getClientID() {
        return env.get("OIDC_CLIENT_ID");
    }

    public String getAuthorizeURL() {
        return env.get("OIDC_AUTHORIZE_URL");
    }

    public String getCallbackURL() {
        return env.get("OIDC_CALLBACK_URL");
    }

    public String getAuthFailureRedirectUri() {
        return env.get("AUTH_FAILURE_REDIRECT_URI");
    }
}
