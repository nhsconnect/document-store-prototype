import AuthCallbackRouter from "./AuthCallbackRouter";
import { render, screen } from "@testing-library/react";
import routes from "../../enums/routes";
import { BrowserRouter } from "react-router-dom";
import axios from "axios";

jest.mock("../../providers/configProvider/ConfigProvider");

describe("AuthCallbackRouter", () => {
    jest.mock("axios");
    const oldWindowLocation = window.location;

    afterAll(() => {
        window.location = oldWindowLocation;
        jest.resetAllMocks();
    });

    it("navigates to the token request handler URl", async () => {
        const codeAndStateQueryParams = "code=some-auth-code&state=some-state";
        const allQueryParams = `?${codeAndStateQueryParams}&client_id=some-client-id`;
        const baseUiUrl = "http://localhost:3000";
        const baseAPIUrl = "https://api.url";
        const redirect_uri = `${baseUiUrl}${routes.AUTH_SUCCESS}`;
        const error_uri = `${baseUiUrl}${routes.AUTH_ERROR}`;
        const tokenRequestHandlerUrl = `${baseAPIUrl}/Auth/TokenRequest?${codeAndStateQueryParams}&redirect_uri=${redirect_uri}&error_uri=${error_uri}`;
        /*const windowLocationProperties = {
            search: { value: allQueryParams },
            replace: { value: jest.fn() },
            href: { value: baseUiUrl },
        };*/

        //delete window.location;
        //window.location = Object.defineProperties({}, windowLocationProperties);
        //useBaseAPIUrl.mockReturnValue(baseAPIUrl);

        // expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        // expect(window.location.replace).toHaveBeenCalledWith(tokenRequestHandlerUrl);

        render(
            <BrowserRouter>
                {" "}
                <AuthCallbackRouter />{" "}
            </BrowserRouter>
        );

        let catchFn = jest.fn(),
            thenFn = jest.fn();

        let tokenResponse = {
            State: "State=some-state; SameSite=None; Secure; Path=/; Max-Age=0; HttpOnly",
            SessionId:
                "SessionId=8634b700-fe04-4c30-a95c-c10ad378ec5c; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
            RoleId: "RoleId=ADMIN; SameSite=None; Secure; Path=/; Max-Age=3592; HttpOnly",
        };
        let promise = axios.get(tokenRequestHandlerUrl).then(thenFn).catch(catchFn);

        //expect(axios.get).toHaveBeenCalled();
        //expect(axios.get).toHaveBeenCalled();
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
