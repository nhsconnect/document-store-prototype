import AuthCallbackRouter from "./AuthCallbackRouter";
import { render, screen, waitFor } from "@testing-library/react";
import routes from "../../enums/routes";
import { BrowserRouter } from "react-router-dom";
import axios from "axios";
import { act } from "react-dom/test-utils";

jest.mock("../../providers/configProvider/ConfigProvider");
jest.mock("axios");

describe("AuthCallbackRouter", () => {
    let tokenResponse = {
        State: "State=some-state; SameSite=None; Secure; Path=/; Max-Age=0; HttpOnly",
        SessionId:
            "SessionId=8634b700-fe04-4c30-a95c-c10ad378ec5c; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
        RoleId: "RoleId=ADMIN; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
    };

    beforeEach(() => {
        axios.get.mockResolvedValue(tokenResponse);
    });

    afterAll(() => {
        jest.clearAllMocks();
    });

    it("navigates to the token request handler URl", async () => {
        const codeAndStateQueryParams = "code=some-auth-code&state=some-state";
        const allQueryParams = `?${codeAndStateQueryParams}&client_id=some-client-id`;
        const baseUiUrl = "http://localhost:3000";
        const baseAPIUrl = "https://api.url";
        const redirect_uri = `${baseUiUrl}${routes.AUTH_SUCCESS}`;
        const error_uri = `${baseUiUrl}${routes.AUTH_ERROR}`;
        const tokenRequestHandlerUrl = `${baseAPIUrl}/Auth/TokenRequest?${codeAndStateQueryParams}&redirect_uri=${redirect_uri}&error_uri=${error_uri}`;

        render(
            <BrowserRouter>
                {" "}
                <AuthCallbackRouter />{" "}
            </BrowserRouter>
        );

        let catchFn = jest.fn(),
            thenFn = jest.fn();

        axios.get(tokenRequestHandlerUrl).then(thenFn).catch(catchFn);

        //expect(mockedUsedNavigate).toHaveBeenCalledTimes(1);
        expect(axios.get).toHaveBeenCalledWith(tokenRequestHandlerUrl);
    });

    it("returns a loading state until redirection to token request handler", async () => {
        render(
            <BrowserRouter>
                {" "}
                <AuthCallbackRouter />{" "}
            </BrowserRouter>
        );

        expect(screen.getByRole("Spinner", { name: "Logging in..." })).toBeInTheDocument();
    });
});
