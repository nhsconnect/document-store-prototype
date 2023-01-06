import { ButtonLink, ErrorSummary } from "nhsuk-react-components";
import React, { useEffect } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "react-oidc-context";

const Authenticator = {};

const Errors = ({ title = "There is a problem" }) => {
    const { error } = useAuth();

    return error ? (
        <ErrorSummary>
            <ErrorSummary.Title id="error-summary-title">{title}</ErrorSummary.Title>
            <ErrorSummary.Body>
                <p>Technical error - Please retry</p>
            </ErrorSummary.Body>
        </ErrorSummary>
    ) : null;
};
Authenticator.Errors = Errors;

const Protected = ({ children }) => {
    const auth = useAuth();
    const { isAuthenticated, isLoading, error, signinRedirect } = auth;

    useEffect(() => {
        if (!isAuthenticated && !isLoading && !error) {
            void signinRedirect();
        }
    }, [isAuthenticated, isLoading, error, signinRedirect]);

    return <>{isAuthenticated && children}</>;
};
Authenticator.Protected = Protected;

const LogOut = () => {
    const { isAuthenticated, removeUser } = useAuth();
    const navigate = useNavigate();

    const signOut = () => {
        removeUser();
        navigate("/");
    };

    if (isAuthenticated) {
        return (
            <ButtonLink secondary onClick={() => signOut()}>
                Log Out
            </ButtonLink>
        );
    }
    return null;
};
Authenticator.LogOut = LogOut;

export default Authenticator;
