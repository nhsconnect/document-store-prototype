import React, { useEffect } from "react";
import { Header } from "nhsuk-react-components";
import { useAuth } from "react-oidc-context";
import ServiceError from "../serviceError/ServiceError";
import routes from "../../enums/routes";
import { useNavigate } from "react-router";

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

const NavLinks = () => {
    const { isAuthenticated, removeUser } = useAuth();
    const navigate = useNavigate();

    const logout = async (event) => {
        event.preventDefault();

        navigate(routes.ROOT);
        await removeUser();
    };

    return isAuthenticated ? (
        <>
            <Header.NavItem href={routes.HOME}>Home</Header.NavItem>
            <Header.NavItem href={routes.ROOT} onClick={logout}>
                Log Out
            </Header.NavItem>
        </>
    ) : null;
};

const Authenticator = { Errors, Protected, NavLinks };

export default Authenticator;
