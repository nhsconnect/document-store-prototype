package uk.nhs.digital.docstore.authoriser;

import java.util.Map;

public class OIDCClientConfig {
    private final String clientID;
    private final String authorizeURL;
    private final String callbackURL;

    public OIDCClientConfig(Map<String, String> env) {
        clientID = env.get("OIDC_CLIENT_ID");
        authorizeURL = env.get("OIDC_AUTHORIZE_URL");
        callbackURL = env.get("OIDC_CALLBACK_URL");
    }

    public OIDCClientConfig() {
        this(System.getenv());
    }

    public String getClientID() {
        return clientID;
    }

    public String getAuthorizeURL() {
        return authorizeURL;
    }

    public String getCallbackURL() {
        return callbackURL;
    }
}
