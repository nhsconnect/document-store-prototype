import { render, screen, waitFor } from "@testing-library/react";
import AuthCallbackRouter from "./AuthCallbackRouter";
import { useNavigate } from "react-router";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import routes from "../../enums/routes";
import axios from "axios";

jest.mock("react-router");
jest.mock("../../providers/configProvider/ConfigProvider");
jest.mock("axios");

describe("AuthCallbackRouter", () => {
    const oldWindowLocation = window.location;

    afterAll(() => {
        window.location = oldWindowLocation;
    });

    it("navigates to the token request handler URl", async () => {
        const codeAndStateQueryParams = "code=some-auth-code&state=some-state";
        const allQueryParams = `?${codeAndStateQueryParams}&client_id=some-client-id`;
        const baseUiUrl = "http://localhost:3000";
        const baseAPIUrl = "https://api.url";

        const windowLocationProperties = {
            search: { value: allQueryParams },
            replace: { value: jest.fn() },
            href: { value: baseUiUrl },
        };

        delete window.location;
        window.location = Object.defineProperties({}, windowLocationProperties);
        useBaseAPIUrl.mockReturnValue(baseAPIUrl);

        // Mock the GET request
        const responseData = {
            State: "State=some-state; SameSite=None; Secure; Path=/; Max-Age=0; HttpOnly",
            SessionId:
                "SessionId=8634b700-fe04-4c30-a95c-c10ad378ec5c; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
            RoleId: "RoleId=ADMIN; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
        };
        const params = {
            code: "some-auth-code",
            state: "some-state",
            error_uri: new URL(`${baseUiUrl}/auth-error`),
            redirect_uri: new URL(`${baseUiUrl}/auth-success`),
        };
        axios.get.mockResolvedValue(responseData);

        // Create a mock navigate function
        const mockNavigate = jest.fn();
        useNavigate.mockImplementation(() => mockNavigate);

        render(<AuthCallbackRouter />);

        // Wait for the navigation to occur
        await waitFor(() => {
            expect(axios.get).toHaveBeenCalledWith(`${baseAPIUrl}/Auth/TokenRequest`, {
                params,
                withCredentials: true,
            });
            expect(axios.get).toHaveBeenCalledTimes(1);
            expect(mockNavigate).toHaveBeenCalledWith(routes.AUTH_SUCCESS);
        });
    });

    it("returns a loading state until redirection to token request handler", async () => {
        render(<AuthCallbackRouter />);

        expect(screen.getByRole("Spinner", { name: "Logging in..." })).toBeInTheDocument();
    });
});
