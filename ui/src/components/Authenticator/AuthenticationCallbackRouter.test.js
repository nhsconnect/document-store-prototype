import { render, screen } from "@testing-library/react";
import { Navigate } from "react-router";
import { useAuth } from "react-oidc-context";
import AuthenticationCallbackRouter from "./AuthenticationCallbackRouter";

jest.mock("react-router");
jest.mock("react-oidc-context");

describe("CIS2AuthenticationResultNavigator", () => {
    it("navigates to home page when authentication is successful", () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            isLoading: false,
            error: false,
        }));

        Navigate.mockImplementation(() => null);
        render(<AuthenticationCallbackRouter />);

        expect(Navigate).toBeCalledWith(
            expect.objectContaining({ to: "/home", replace: true }),
            expect.anything()
        );
    });

    it("navigates to start page when authentication is unsuccessful", () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: false,
            isLoading: false,
            error: true,
        }));

        Navigate.mockImplementation(() => null);
        render(<AuthenticationCallbackRouter />);

        expect(Navigate).toBeCalledWith(
            expect.objectContaining({ to: "/", replace: true }),
            expect.anything()
        );
    });

    it("displays loading if authentication is incomplete", () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            isLoading: true,
            error: true,
        }));

        Navigate.mockImplementation(() => null);
        render(<AuthenticationCallbackRouter />);

        expect(screen.getByText("Loading...")).toBeInTheDocument();
    });
});
