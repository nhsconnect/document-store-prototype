import { ErrorSummary } from "nhsuk-react-components";
import React, { useEffect } from "react";

import { Link } from "react-router-dom";
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

const linkStyle = { color: "#FFFFFF", position: "absolute", right: 0, top: 0, minWidth: 150 };

const LogOut = () => {
    const { isAuthenticated, removeUser } = useAuth();

    const signOut = () => {
        removeUser();
    };

    if (isAuthenticated) {
        return (
            <Link to="/" onClick={() => signOut()} style={linkStyle}>
                Log Out
            </Link>
        );
    }
    return null;
};
Authenticator.LogOut = LogOut;

export default Authenticator;
