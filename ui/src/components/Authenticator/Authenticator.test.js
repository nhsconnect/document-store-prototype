import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Auth, Hub } from "aws-amplify";
import React from "react";
import { useLocation } from "react-router";

import config from "../../config";
import * as FeatureToggleProvider from "../../providers/FeatureToggleProvider";
import Authenticator from "./Authenticator";
import {AmplifySignOut} from "@aws-amplify/ui-react";

jest.mock("react-router", () => ({
    useLocation: jest.fn(),
}));
jest.mock("@aws-amplify/ui-react", () => ({
    ...jest.requireActual("@aws-amplify/ui-react"),
    AmplifyAuthenticator: ({ children, handleAuthStateChange }) => {
        return (
            <>
                <button
                    onClick={() => {
                        handleAuthStateChange("signedin");
                    }}
                >
                    Sign in
                </button>
                <div>{children}</div>
            </>
        );
    },
}));

async function expectNever(callable) {
    await expect(() => waitFor(callable)).rejects.toEqual(expect.anything());
}

describe("Authenticator", () => {
    describe("CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED feature toggle is active", () => {
        let defaultAuthConfig;
        let federatedSignIn;

        beforeAll(() => {
            defaultAuthConfig = config.Auth;
            config.Auth.oauth = {
                domain: "doc-store-user-pool.auth.eu-west-2.amazoncognito.com",
                scope: ["openid"],
                redirectSignIn: "http://localhost:3000",
                redirectSignOut: "",
                responseType: "code",
            };
        });

        afterAll(() => {
            config.Auth = defaultAuthConfig;
        });

        beforeEach(() => {
            jest.spyOn(Auth, "currentSession").mockImplementation(async () => ({
                idToken: {
                    jwtToken: "a-token",
                },
            }));
            federatedSignIn = jest.spyOn(Auth, "federatedSignIn");
            federatedSignIn.mockImplementation(async () => true);
            jest.spyOn(
                FeatureToggleProvider,
                "useFeatureToggle"
            ).mockImplementation(
                (toggleName) =>
                    toggleName === "CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED"
            );
            useLocation.mockImplementation(() => ({}));
        });

        it("does not render children when user IS NOT authenticated", async () => {
            jest.spyOn(Auth, "currentSession").mockImplementation(
                async () => false
            );
            render(
                <Authenticator>
                    <Authenticator.Protected>
                        <div>this-should-NOT-be-rendered</div>
                    </Authenticator.Protected>
                </Authenticator>
            );
            await expectNever(() => {
                expect(
                    screen.queryByText("this-should-NOT-be-rendered")
                ).toBeInTheDocument();
            });
        });

        it("renders children when user IS authenticated", async () => {
            render(
                <Authenticator>
                    <Authenticator.Protected>
                        <div>this-should-be-rendered</div>
                    </Authenticator.Protected>
                </Authenticator>
            );
            act(() => {
                Hub.dispatch("auth", {
                    event: "signIn",
                    data: {},
                });
            });
            await waitFor(() => {
                expect(
                    screen.queryByText("this-should-be-rendered")
                ).toBeInTheDocument();
            });
        });

        it("displays an error summary when authentication fails", async () => {
            render(
                <Authenticator>
                    <Authenticator.Errors />
                </Authenticator>
            );

            act(() => {
                Hub.dispatch("auth", {
                    event: "signIn_failure",
                    data: {
                        error_description: "invalid_scope",
                        error: "invalid_token",
                    },
                });
            });

            await waitFor(() => {
                expect(
                    screen.queryByText("Technical error - Please retry")
                ).toBeInTheDocument();
            });
        });

        it("display an error summary when authentication redirects with an error", async () => {
            useLocation.mockImplementation(() => ({
                hash: "",
                search: "?error=invalid_scope",
            }));
            render(
                <Authenticator>
                    <Authenticator.Errors />
                </Authenticator>
            );
            await waitFor(() => {
                expect(
                    screen.queryByText("Technical error - Please retry")
                ).toBeInTheDocument();
            });
        });

        it("display an error summary when authentication redirects with an error in hash", async () => {
            useLocation.mockImplementation(() => ({
                hash: "#error=invalid_scope",
            }));
            render(
                <Authenticator>
                    <Authenticator.Errors />
                </Authenticator>
            );
            await waitFor(() => {
                expect(
                    screen.queryByText("Technical error - Please retry")
                ).toBeInTheDocument();
            });
        });

        it("does not call federatedSignIn if access token already exists", async () => {
            act(() => {
                render(
                    <Authenticator>
                        <Authenticator.Protected>
                            <div>this-should-be-rendered</div>
                        </Authenticator.Protected>
                    </Authenticator>
                );
            });
            await waitFor(() => {
                expect(federatedSignIn).not.toHaveBeenCalled();
                expect(
                    screen.queryByText("this-should-be-rendered")
                ).toBeInTheDocument();
            });
        });

        it("does not call federatedSignIn if unauthenticated user attempts to access protected route", async () => {
            jest.spyOn(Auth, "currentSession").mockImplementation(
                async () => false
            );
            render(
                <Authenticator>
                    <Authenticator.Protected>
                        <div>this-should-NOT-be-rendered</div>
                    </Authenticator.Protected>
                </Authenticator>
            );

            await waitFor(() => {
                expect(federatedSignIn).toHaveBeenCalled();
            });
        });

        it("does not try to redirect back to CIS2 when authentication redirects with an error", async () => {
            jest.spyOn(Auth, "currentSession").mockImplementation(
                async () => false
            );
            useLocation.mockImplementation(() => ({
                hash: "",
                search: "?error=invalid_scope",
            }));
            render(
                <Authenticator>
                    <Authenticator.Errors />
                    <Authenticator.Protected>
                        <div>this-should-NOT-be-rendered</div>
                    </Authenticator.Protected>
                </Authenticator>
            );
            await waitFor(() => {
                expect(
                    screen.getByText("Technical error - Please retry")
                ).toBeInTheDocument();
            });
            expect(federatedSignIn).not.toHaveBeenCalled();
        });
    });

    describe("CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED feature toggle is inactive", () => {
        beforeEach(() => {
            jest.spyOn(
                FeatureToggleProvider,
                "useFeatureToggle"
            ).mockImplementation(() => false);
        });

        it("does not render children when user IS NOT authenticated", () => {
            render(
                <Authenticator>
                    <Authenticator.Protected>
                        <div>this-should-NOT-be-rendered</div>
                    </Authenticator.Protected>
                </Authenticator>
            );
            expect(
                screen.queryByText("this-should-NOT-be-rendered")
            ).not.toBeInTheDocument();
        });

        it("renders children when user IS authenticated", async () => {
            render(
                <Authenticator>
                    <Authenticator.Protected>
                        <div>this-should-be-rendered</div>
                    </Authenticator.Protected>
                </Authenticator>
            );

            userEvent.click(screen.getByText("Sign in"));

            await waitFor(() => {
                expect(
                    screen.queryByText("this-should-be-rendered")
                ).toBeInTheDocument();
            });
        });

        it("renders the amplify sign out button when user is authenticated", () => {
            render(
                <Authenticator>
                    <Authenticator.SignOut />
                </Authenticator>
            );

            userEvent.click(screen.getByText("Sign in"));

            expect(screen.getByText("Sign out")).toBeInTheDocument();
        });

        it("does not render the amplify sign out button when user is not authenticated", () => {
            render(
                <Authenticator>
                    <Authenticator.SignOut />
                </Authenticator>
            );

            expect(screen.queryByText("Sign out")).not.toBeInTheDocument();
        });
    });
});
