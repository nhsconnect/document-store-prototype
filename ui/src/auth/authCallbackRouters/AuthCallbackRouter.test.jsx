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
    const codeAndStateQueryParams = "code=some-auth-code&state=some-state";
    const allQueryParams = `?${codeAndStateQueryParams}&client_id=some-client-id`;
    const baseUiUrl = "http://localhost:3000";
    const baseAPIUrl = "https://api.url";

    const windowLocationProperties = {
        search: { value: allQueryParams },
        replace: { value: jest.fn() },
        href: { value: baseUiUrl },
    };

    const params = {
        code: "some-auth-code",
        state: "some-state",
    };

    const mockNavigate = jest.fn();

    beforeEach(() => {
        delete window.location;
        window.location = Object.defineProperties({}, windowLocationProperties);
        useBaseAPIUrl.mockReturnValue(baseAPIUrl);
        useNavigate.mockImplementation(() => mockNavigate);
    });

    afterAll(() => {
        window.location = oldWindowLocation;
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    it("navigates to the token request handler URl", async () => {
        // Mock the GET request
        const responseData = {
            State: "State=some-state; SameSite=None; Secure; Path=/; Max-Age=0; HttpOnly",
            SessionId:
                "SessionId=8634b700-fe04-4c30-a95c-c10ad378ec5c; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
            RoleId: "RoleId=ADMIN; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
        };

        axios.get.mockResolvedValue(responseData);

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

    it("navigates to the no valid organisation page when response status code is 401", async () => {
        // Mock the expected error response
        const errorResponse = {
            response: {
                status: 401,
                message: "401 unauthorised",
            },
        };

        axios.get.mockRejectedValueOnce(errorResponse);

        render(<AuthCallbackRouter />);

        // Wait for the navigation to occur
        await waitFor(() => {
            expect(axios.get).toHaveBeenCalledWith(`${baseAPIUrl}/Auth/TokenRequest`, {
                params,
                withCredentials: true,
            });
            expect(axios.get).toHaveBeenCalledTimes(1);
            expect(mockNavigate).toHaveBeenCalledWith(routes.NO_VALID_ORGANISATION);
        });
    });

    it("navigates to the auth error page when response status code is 403", async () => {
        // Mock the expected error response
        const errorResponse = {
            response: {
                status: 403,
                message: "403 forbidden",
            },
        };

        axios.get.mockRejectedValueOnce(errorResponse);

        render(<AuthCallbackRouter />);

        // Wait for the navigation to occur
        await waitFor(() => {
            expect(axios.get).toHaveBeenCalledWith(`${baseAPIUrl}/Auth/TokenRequest`, {
                params,
                withCredentials: true,
            });
            expect(axios.get).toHaveBeenCalledTimes(1);
            expect(mockNavigate).toHaveBeenCalledWith(routes.AUTH_ERROR);
        });
    });
});
