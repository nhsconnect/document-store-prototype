import { render, screen } from "@testing-library/react";
import { Navigate } from "react-router";
import { useAuth } from "react-oidc-context";
import CognitoAuthCallbackRouter from "./CognitoAuthCallbackRouter";

jest.mock("react-router");
jest.mock("react-oidc-context");

describe("<CognitoAuthCallbackRouter />", () => {
    it("navigates to /home when auth is successful", () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            isLoading: false,
            error: false,
        }));
        Navigate.mockImplementation(() => null);

        render(<CognitoAuthCallbackRouter />);

        expect(Navigate).toBeCalledWith(expect.objectContaining({ to: "/home", replace: true }), expect.anything());
    });

    it("navigates to start page when auth is unsuccessful", () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: false,
            isLoading: false,
            error: true,
        }));
        Navigate.mockImplementation(() => null);

        render(<CognitoAuthCallbackRouter />);

        expect(Navigate).toBeCalledWith(expect.objectContaining({ to: "/", replace: true }), expect.anything());
    });

    it("displays loading if auth is incomplete", () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            isLoading: true,
            error: true,
        }));
        Navigate.mockImplementation(() => null);

        render(<CognitoAuthCallbackRouter />);

        expect(screen.getByText("Loading...")).toBeInTheDocument();
    });
});
