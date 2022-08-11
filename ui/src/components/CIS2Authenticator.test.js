import {render, screen, waitFor} from "@testing-library/react";
import CIS2Authenticator from "./CIS2Authenticator";
import {Auth, Hub} from "aws-amplify";
import config from "../config";
import * as FeatureToggleProvider from "../providers/FeatureToggleProvider";

jest.mock("react-router", () => ({
    useLocation: jest.fn()
}));

import { useLocation } from "react-router";
import Authenticator from "./Authenticator";
import React from "react";

describe("CIS2Authenticator", () => {

    let defaultAuthConfig;

    beforeAll(() => {
        defaultAuthConfig = config.Auth;
        config.Auth.oauth = {
            domain: "doc-store-user-pool.auth.eu-west-2.amazoncognito.com",
            scope: ["openid"],
            redirectSignIn: "http://localhost:3000",
            redirectSignOut: "",
            responseType: "code"
        }
    });

    afterAll(() => {
        config.Auth = defaultAuthConfig;
        jest.clearAllMocks();
    });

    beforeEach(() => {
        jest.spyOn(Auth, 'currentSession')
            .mockImplementation(async () => ({
                idToken: {
                    jwtToken: "a-token"
                }
            }));
        jest.spyOn(Auth, 'federatedSignIn')
            .mockImplementation(async () => true);
        jest.spyOn(FeatureToggleProvider, 'useFeatureToggle')
            .mockImplementation(() => true);
        useLocation.mockImplementation(() => ({}));
    })

    it("Given user IS NOT authenticated then no children should be rendered", () => {
        render(<Authenticator>
            <Authenticator.Protected>
                <div>this-should-NOT-be-rendered</div>
            </Authenticator.Protected>
        </Authenticator>);
        expect(screen.queryByText("this-should-NOT-be-rendered")).not.toBeInTheDocument();
        screen.debug();
    });

    it("Given user IS authenticated then children should be rendered", async () => {
        render(<Authenticator>
            <Authenticator.Protected>
                <div>this-should-be-rendered</div>
            </Authenticator.Protected>
        </Authenticator>);
        Hub.dispatch('auth', {
            event: 'signIn',
            data: {}
        });
        await waitFor(() => {
            expect(screen.queryByText("this-should-be-rendered")).toBeInTheDocument();
        });
    });

    it("Given authentication failed then display an error summery", async () => {
        render(<Authenticator><Authenticator.Errors /></Authenticator>);
        Hub.dispatch('auth', {
            event: 'signIn_failure',
            data: {
                "error_description":"The access token provided is expired, revoked, malformed, or invalid for other reasons.",
                "error": "invalid_token"
            }
        });
        expect(screen.queryByText("The access token provided is expired, revoked, malformed, or invalid for other reasons.")).toBeInTheDocument();
    });

    it("Given redirected with error then display an error summery", async () => {
        useLocation.mockImplementation(() => ({
            search: "?error_description=The%20access%20token%20provided%20is%20expired%2C%20revoked%2C%20malformed%2C%20or%20invalid%20for%20other%20reasons."
        }));
        render(<Authenticator><Authenticator.Errors /></Authenticator>);
        await waitFor(() => {
            expect(screen.queryByText("The access token provided is expired, revoked, malformed, or invalid for other reasons.")).toBeInTheDocument();
        });
    });

});