import { deleteFromStorage, writeStorage } from "@rehooks/local-storage";
import { Auth, Hub } from "aws-amplify";
import { useContext, useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router";

import awsConfig from "../../config";
import AuthenticationContext from "../../providers/AuthenticatorErrorsProvider";

const getToken = async () => {
    try {
        const session = await Auth.currentSession();
        return session?.idToken?.jwtToken;
    } catch (e) {
        return undefined;
    }
};

export function useQuery() {
    const { search, hash } = useLocation();

    return useMemo(() => {
        if (hash !== undefined && hash !== "") {
            return new URLSearchParams(hash.replace("#", ""));
        }
        return new URLSearchParams(search);
    }, [search, hash]);
}

const attemptFederatedLogin = (query, tryLogin, setError, setTryLogin) => async () => {

    if (query.get("error_description")) {
        setError({ error_description: query.get("error_description") });
        setTryLogin(false);
        return;
    }

    if (tryLogin) {
        try {
            const userToken = await getToken();
            if (!userToken) {
                await Auth.federatedSignIn({
                    provider: awsConfig.Auth.providerId,
                });
            }
        } catch (e) {
            setError(e);
        }
        setTryLogin(false);
    }
};

function checkAuthenticated(setIsAuthenticated, setError) {
    (async () => {
        try {
            const userToken = await getToken();
            if (userToken) {
                setIsAuthenticated(true);
            }
        } catch (e) {
            setError(e);
        }
    })();
}

function onFirstRender(query, setError, setIsAuthenticated, authHandler) {
    return () => {
        checkAuthenticated(setIsAuthenticated, setError);

        return Hub.listen("auth", authHandler);
    };
}

const CIS2Authenticator = ({ children }) => {
    const query = useQuery();
    const { setError, setIsAuthenticated, attemptLogin, setAttemptLogin } =
        useContext(AuthenticationContext);

    const authHandler = async ({ payload: { event, data } }) => {
        switch (event) {
            case "signIn":
            case "cognitoHostedUI":
                setIsAuthenticated(true);
                break;
            case "signOut":
                break;
            case "signIn_failure":
            case "cognitoHostedUI_failure":
            default:
                if (data === undefined) {
                    data = { error_description: "There was a problem" };
                }
                setError(data);
                break;
        }
    };

    useEffect(
        onFirstRender(query, setError, setIsAuthenticated, authHandler),
        []
    );

    useEffect(attemptFederatedLogin(query, attemptLogin, setError, setAttemptLogin), [
        attemptLogin,
    ]);

    return <div data-testid={"CIS2Authenticator"}>{children}</div>;
};

export default CIS2Authenticator;
