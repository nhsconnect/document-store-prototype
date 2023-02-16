import React, { useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import ServiceError from "../ServiceError";

const Errors = () => {
    const { error } = useAuth();

    return error ? <ServiceError message="Sorry, we can't log you in at the moment. Please try again later." /> : null;
};

const Protected = ({ children }) => {
    const { isAuthenticated, isLoading, error, signinRedirect } = useAuth();

    useEffect(() => {
        if (!isAuthenticated && !isLoading && !error) {
            void signinRedirect();
        }
    }, [isAuthenticated, isLoading, error, signinRedirect]);

    return <>{isAuthenticated && children}</>;
};

const LogOut = () => {
    const { isAuthenticated, removeUser } = useAuth();

    const linkStyle = { color: "#FFFFFF", position: "absolute", right: 0, top: 0, minWidth: 150 };

    return isAuthenticated ? (
        <Link to="/" onClick={() => removeUser()} style={linkStyle}>
            Log Out
        </Link>
    ) : null;
};

const Authenticator = { Errors, Protected, LogOut };

export default Authenticator;
