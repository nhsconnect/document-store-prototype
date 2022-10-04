import { Auth, Hub } from "aws-amplify";
import { useContext, useEffect, useMemo } from "react";
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

/**
 * Extracts information returned by CIS2 in the url. Most likely errors. Sometimes, these are in the query and sometimes they're in the hash.
 * @returns URLSearchParams
 */
export function useQuery() {
    const { search, hash } = useLocation();

    return useMemo(() => {
        if (hash !== undefined && hash !== "") {
            return new URLSearchParams(hash.replace("#", ""));
        }
        return new URLSearchParams(search);
    }, [search, hash]);
}

const attemptFederatedLogin =
    (query, attemptLogin, setError, setAttemptLogin) => async () => {
        if (query.has("error")) {
            setError(true);
            setAttemptLogin(false);
            return;
        }

        if (attemptLogin) {
            try {
                const userToken = await getToken();
                if (!userToken) {
                    await Auth.federatedSignIn({
                        provider: awsConfig.Auth.providerId,
                    });
                }
            } catch (e) {
                setError(true);
            }
            setAttemptLogin(false);
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
            setError(true);
        }
    })();
}

/* on first render, we want to check whether a user is authenticated and set the value in the parent authenticator context.
We also want to set up a listener to listen to future authentication events.
More information can be found here: https://docs.amplify.aws/guides/authentication/listening-for-auth-events/q/platform/js/*/
function onFirstRender(setError, setIsAuthenticated, authHandler) {
    return () => {
        checkAuthenticated(setIsAuthenticated, setError);

        return Hub.listen("auth", authHandler);
    };
}

const CIS2Authenticator = ({ children }) => {
    const query = useQuery();
    const { setError, setIsAuthenticated, attemptLogin, setAttemptLogin } =
        useContext(AuthenticationContext);

    useEffect(() => {
        const authHandler = async ({ payload: { event, data } }) => {
            switch (event) {
                case "signIn":
                case "cognitoHostedUI":
                    setIsAuthenticated(true);
                    break;
                case "signOut":
                    console.log("**signing out**");
                    setIsAuthenticated(false);
                    break;
                case "signIn_failure":
                case "cognitoHostedUI_failure":
                default:
                    setError(true);
                    break;
            }
        };
        return onFirstRender(setError, setIsAuthenticated, authHandler)();
    }, [setError, setIsAuthenticated]);

    useEffect(
        () =>
            attemptFederatedLogin(
                query,
                attemptLogin,
                setError,
                setAttemptLogin
            )(),
        [attemptLogin, query, setAttemptLogin, setError]
    );

    return <div data-testid={"CIS2Authenticator"}>{children}</div>;
};

export default CIS2Authenticator;
