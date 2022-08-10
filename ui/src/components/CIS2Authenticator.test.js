import {render, screen, waitFor} from "@testing-library/react";
import CIS2Authenticator from "./CIS2Authenticator";
import {Auth, Hub} from "aws-amplify";
import {act} from "react-dom/test-utils";
import config from "../config";

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

        jest.spyOn(Auth, 'currentSession')
            .mockImplementation(() => new Promise((resolve) => resolve({
                idToken: {
                    jwtToken: "a-token"
                }
            })));
        jest.spyOn(Auth, 'federatedSignIn')
            .mockImplementation(() => new Promise((resolve) => resolve(true)));
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
    })

    it("Given user IS NOT authenticated then no children should be rendered", () => {
        render(<CIS2Authenticator><div>this-should-NOT-be-rendered</div></CIS2Authenticator>);
        expect(screen.queryByText("this-should-NOT-be-rendered")).not.toBeInTheDocument();
    });

    it("Given user IS authenticated then children should be rendered", async () => {
        render(<CIS2Authenticator><div>this-should-be-rendered</div></CIS2Authenticator>);
        Hub.dispatch('auth', {
            event: 'signIn',
            data: {}
        });
        await waitFor(() => {
            expect(screen.queryByText("this-should-be-rendered")).toBeInTheDocument();
        });
    });

});