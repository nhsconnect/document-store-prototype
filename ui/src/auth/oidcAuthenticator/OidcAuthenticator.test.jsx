import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import React from "react";
import { useAuth } from "react-oidc-context";
import OidcAuthenticator from "./OidcAuthenticator";
import routes from "../../enums/routes";
import { MemoryRouter } from "react-router";

jest.mock("react-oidc-context");

describe("OidcAuthenticator", () => {
    it("does not render children when user IS NOT authenticated", async () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: false,
            isLoading: true,
            error: true,
            signinRedirect: jest.fn(),
        }));

        render(
            <OidcAuthenticator.Protected>
                <div>this-should-NOT-be-rendered</div>
            </OidcAuthenticator.Protected>
        );

        await expectNever(() => {
            expect(screen.queryByText("this-should-NOT-be-rendered")).toBeInTheDocument();
        });
    });

    it("renders children when user IS authenticated", async () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            isLoading: true,
            error: true,
            signinRedirect: jest.fn(),
        }));

        render(
            <OidcAuthenticator.Protected>
                <div>this-should-be-rendered</div>
            </OidcAuthenticator.Protected>
        );

        await waitFor(() => {
            expect(screen.queryByText("this-should-be-rendered")).toBeInTheDocument();
        });
    });

    it("does not render children while the authentication is incomplete", async () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: false,
            isLoading: true,
            error: false,
            signinRedirect: jest.fn(),
        }));

        render(
            <OidcAuthenticator.Protected>
                <div>this-should-NOT-be-rendered</div>
            </OidcAuthenticator.Protected>
        );

        await expectNever(() => {
            expect(screen.queryByText("this-should-NOT-be-rendered")).toBeInTheDocument();
        });
    });

    it("displays an error summary when authentication fails", async () => {
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            isLoading: true,
            error: true,
            signinRedirect: jest.fn(),
        }));

        render(<OidcAuthenticator.Errors />);

        await waitFor(() => {
            expect(
                screen.queryByText("Sorry, we can't log you in at the moment. Please try again later.")
            ).toBeInTheDocument();
        });
    });

    it("does not redirect if the user is already authenticated", async () => {
        const redirectFunction = jest.fn();

        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            isLoading: true,
            error: true,
            signinRedirect: redirectFunction,
        }));

        render(
            <OidcAuthenticator.Protected>
                <div>this-should-be-rendered</div>
            </OidcAuthenticator.Protected>
        );

        await waitFor(() => {
            expect(redirectFunction).not.toHaveBeenCalled();
            expect(screen.getByText("this-should-be-rendered")).toBeInTheDocument();
        });
    });

    it("does redirect if an unauthenticated user attempts to access a protected route", async () => {
        const redirectFunction = jest.fn();

        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: false,
            isLoading: false,
            error: false,
            signinRedirect: redirectFunction,
        }));

        render(
            <OidcAuthenticator.Protected>
                <div>this-should-NOT-be-rendered</div>
            </OidcAuthenticator.Protected>
        );

        await waitFor(() => {
            expect(redirectFunction).toHaveBeenCalled();
        });
    });

    it("does not redirect the user when the authentication redirects with an error", async () => {
        const redirectFunction = jest.fn();

        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: false,
            isLoading: false,
            error: true,
            signinRedirect: redirectFunction,
        }));

        render(
            <OidcAuthenticator.Protected>
                <div>this-should-NOT-be-rendered</div>
            </OidcAuthenticator.Protected>
        );

        await expectNever(() => {
            expect(redirectFunction).toHaveBeenCalled();
        });
    });

    it("does not redirect the user while the authentication is incomplete", async () => {
        const redirectFunction = jest.fn();

        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: false,
            isLoading: true,
            error: false,
            signinRedirect: redirectFunction,
        }));

        render(
            <OidcAuthenticator.Protected>
                <div>this-should-NOT-be-rendered</div>
            </OidcAuthenticator.Protected>
        );

        await expectNever(() => {
            expect(redirectFunction).toHaveBeenCalled();
        });
    });

    describe("NavLinks", () => {
        it("renders home and logout link when authenticated", () => {
            useAuth.mockReturnValue({ isAuthenticated: true });

            renderWithRouter(<OidcAuthenticator.NavLinks />);

            expect(screen.getByRole("link", { name: "Home" })).toHaveAttribute("href", routes.HOME);
            expect(screen.getByRole("link", { name: "Log Out" })).toHaveAttribute("href", routes.ROOT);
        });

        it("does not render home and logout link when unauthenticated", () => {
            useAuth.mockReturnValue({ isAuthenticated: false });

            renderWithRouter(<OidcAuthenticator.NavLinks />);

            expect(screen.queryByRole("link", { name: "Home" })).not.toBeInTheDocument();
            expect(screen.queryByRole("link", { name: "Log Out" })).not.toBeInTheDocument();
        });

        it("removes user when logout link is clicked", () => {
            const removeUserMock = jest.fn();

            useAuth.mockReturnValue({ isAuthenticated: true, removeUser: removeUserMock });

            renderWithRouter(<OidcAuthenticator.NavLinks />);

            userEvent.click(screen.getByRole("link", { name: "Log Out" }));

            expect(removeUserMock).toHaveBeenCalled();
        });
    });
});

const expectNever = async (callable) => {
    await expect(() => waitFor(callable)).rejects.toEqual(expect.anything());
};

const renderWithRouter = (component) => {
    render(<MemoryRouter>{component}</MemoryRouter>);
};
