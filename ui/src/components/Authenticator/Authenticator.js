import { AmplifyAuthenticator } from "@aws-amplify/ui-react";
import { ErrorSummary } from "nhsuk-react-components";
import { useContext, useEffect, useState } from "react";

import AuthenticationContext from "../../providers/AuthenticatorErrorsProvider";
import { useFeatureToggle } from "../../providers/FeatureToggleProvider";
import CIS2Authenticator from "./CIS2Authenticator";

const Authenticator = ({ children }) => {
    const [error, setError] = useState(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [attemptLogin, setAttemptLogin] = useState(false);
    const isCIS2FederatedIdentityProviderEnabled = useFeatureToggle(
        "CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED"
    );
    const setIsAuthenticatedOnSignedIn = (status) =>
        setIsAuthenticated(status === "signedin");

    return (
        <AuthenticationContext.Provider
            value={{
                error,
                setError,
                isAuthenticated,
                setIsAuthenticated,
                attemptLogin,
                setAttemptLogin,
            }}
        >
            {isCIS2FederatedIdentityProviderEnabled ? (
                <CIS2Authenticator>{children}</CIS2Authenticator>
            ) : (
                <AmplifyAuthenticator
                    data-testid={"AmplifyAuthenticator"}
                    handleAuthStateChange={setIsAuthenticatedOnSignedIn}
                >
                    {children}
                </AmplifyAuthenticator>
            )}
        </AuthenticationContext.Provider>
    );
};

const Errors = ({ title = "There is a problem" }) => {
    const { error } = useContext(AuthenticationContext);

    return error ? (
        <ErrorSummary>
            <ErrorSummary.Title id="error-summary-title">
                {title}
            </ErrorSummary.Title>
            <ErrorSummary.Body>
                <p>Technical error - Please retry</p>
            </ErrorSummary.Body>
        </ErrorSummary>
    ) : null;
};
Authenticator.Errors = Errors;

/*Prevent users from accessing private pages if they are unauthenticated.
If they are unauthenticated, we set the attemptLogin state to true to tell CIS2 authenticator component that the user should be redirected to the login form. The Amplify Authenticator performs the same function for the Cognito authentication flow. */
const Protected = ({ children }) => {
    const { isAuthenticated, setAttemptLogin } = useContext(
        AuthenticationContext
    );

    useEffect(() => {
        if (!isAuthenticated) {
            setAttemptLogin(true);
        }
    }, [isAuthenticated, setAttemptLogin]);

    return <>{isAuthenticated && children}</>;
};
Authenticator.Protected = Protected;

export default Authenticator;
