import { render, screen, waitFor } from "@testing-library/react";
import AuthCallbackRouter from "./AuthCallbackRouter";
import { useNavigate } from "react-router";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import routes from "../../enums/routes";
import axios from "axios";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

jest.mock("react-router");
jest.mock("../../providers/sessionProvider/SessionProvider");
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

    const session = { isLoggedIn: false };
    const setSessionMock = jest.fn();

    useSessionContext.mockReturnValue([session, setSessionMock]);

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

    it("navigates to the Upload page if user has one valid GPP org", async () => {
        // Mock the GET request
        const responseData = {
            data: {
                Organisations: [{ orgType: "GP Practice", orgName: "PORTWAY LIFESTYLE CENTRE", odsCode: "A9A5A" }],
                UserType: "GP Practice",
            },
        };

        const odsCode = { odsCode: responseData.data.Organisations[0].odsCode };

        axios.get.mockResolvedValue(responseData);

        render(<AuthCallbackRouter />);

        // Wait for the navigation to occur
        await waitFor(() => {
            expect(axios.get).toHaveBeenCalledWith(`${baseAPIUrl}/Auth/TokenRequest`, {
                params,
                withCredentials: true,
            });
            expect(axios.get).toHaveBeenCalledWith(`${baseAPIUrl}/Auth/VerifyOrganisation`, {
                params: odsCode,
                withCredentials: true,
            });

            expect(axios.get).toHaveBeenCalledTimes(2);
            expect(mockNavigate).toHaveBeenCalledWith(routes.UPLOAD_SEARCH_PATIENT);
        });
    });

    it("navigates to the Download page if user has one valid PCSE org", async () => {
        // Mock the GET request
        const responseData = {
            data: {
                Organisations: [
                    { orgType: "Primary Care Support England", orgName: "PCSE DARLINGTON", odsCode: "B1B1B" },
                ],
                UserType: "Primary Care Support England",
            },
        };

        const odsCode = { odsCode: responseData.data.Organisations[0].odsCode };

        axios.get.mockResolvedValue(responseData);

        render(<AuthCallbackRouter />);

        // Wait for the navigation to occur
        await waitFor(() => {
            expect(axios.get).toHaveBeenCalledWith(`${baseAPIUrl}/Auth/TokenRequest`, {
                params,
                withCredentials: true,
            });
            expect(axios.get).toHaveBeenCalledWith(`${baseAPIUrl}/Auth/VerifyOrganisation`, {
                params: odsCode,
                withCredentials: true,
            });

            expect(axios.get).toHaveBeenCalledTimes(2);
            expect(mockNavigate).toHaveBeenCalledWith(routes.SEARCH_PATIENT);
        });
    });

    it("returns a loading state until redirection to token request handler", async () => {
        render(<AuthCallbackRouter />);

        expect(screen.getByRole("Spinner", { name: "Logging in..." })).toBeInTheDocument();
    });

    it("navigates to the auth error page when response status code is 403", async () => {
        useNavigate.mockImplementation(() => mockNavigate);
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

    it("navigates to the org selection page when response status code is 200 and more than one org is included", async () => {
        // Mock the expected response
        const responseData = {
            status: 200,
            data: {
                Organisations: [
                    { orgType: "GP Practice", orgName: "Town GP", odsCode: "A100" },
                    { orgType: "Dev", orgName: "City clinic", odsCode: "A142" },
                    { orgType: "Primary Care Support England", orgName: "National care support", odsCode: "A410" },
                ],
            },
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
            expect(mockNavigate).toHaveBeenCalledWith(routes.ORG_SELECT);
        });
    });
});
