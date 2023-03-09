import { AuthProvider as BaseProvider } from "react-oidc-context";
import config from "../config";
import { useFeatureToggle } from "./ConfigurationProvider";

const oidcConfig = {
    authority: config.Auth.oauth.domain,
    client_id: config.Auth.userPoolWebClientId,
    redirect_uri: config.Auth.oauth.redirectSignIn,
    post_logout_redirect_uri: config.Auth.oauth.redirectSignOut,
    scope: config.Auth.oauth.scope.join(","),
    prompt: "login",
    metadata: {
        issuer: `${config.Auth.oauth.domain}/${config.Auth.userPoolId}`,
        authorization_endpoint: `${config.Auth.oauth.domain}/oauth2/authorize`,
        userinfo_endpoint: `${config.Auth.oauth.domain}/oauth2/userInfo`,
        end_session_endpoint: `${config.Auth.oauth.domain}/logout`,
        token_endpoint: `${config.Auth.oauth.domain}/oauth2/token`,
        jwks_uri: `${config.Auth.oauth.domain}/${config.Auth.userPoolId}/.well-known/jwks.json`,
    },
    extraQueryParams: {
        identity_provider: config.Auth.providerId,
    },
    // No revoke of "access token" (https://github.com/authts/oidc-client-ts/issues/262)
    revokeTokenTypes: ["refresh_token"],
    // No silent renew via "prompt=none" (https://github.com/authts/oidc-client-ts/issues/366)
    automaticSilentRenew: false,
};

const AuthProvider = ({ children }) => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return isOIDCAuthActive ? <BaseProvider {...oidcConfig}>{children}</BaseProvider> : children;
};

export default AuthProvider;
