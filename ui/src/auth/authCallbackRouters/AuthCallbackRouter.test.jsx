import AuthCallbackRouter from "./AuthCallbackRouter";
import { render, screen } from "@testing-library/react";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import routes from "../../enums/routes";

jest.mock("../../providers/configProvider/ConfigProvider");

describe("AuthCallbackRouter", () => {
    const oldWindowLocation = window.location;

    afterAll(() => {
        window.location = oldWindowLocation;
    });

    it("navigates to the token request handler URl", () => {
        const codeAndStateQueryParams = "code=some-auth-code&state=some-state";
        const allQueryParams = `?${codeAndStateQueryParams}&client_id=some-client-id`;
        const baseUiUrl = "http://localhost:3000";
        const baseAPIUrl = "https://api.url";
        const tokenRequestHandlerUrl = `${baseAPIUrl}/Auth/TokenRequest?${codeAndStateQueryParams}&redirect_uri=${baseUiUrl}${routes.AUTH_SUCCESS}`;
        const windowLocationProperties = {
            search: { value: allQueryParams },
            replace: { value: jest.fn() },
            href: { value: baseUiUrl },
        };

        delete window.location;
        window.location = Object.defineProperties({}, windowLocationProperties);
        useBaseAPIUrl.mockReturnValue(baseAPIUrl);

        render(<AuthCallbackRouter />);

        expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        expect(window.location.replace).toHaveBeenCalledWith(tokenRequestHandlerUrl);
    });

    it("returns a loading state until redirection to token request handler", () => {
        render(<AuthCallbackRouter />);

        expect(screen.getByRole("progressbar", { name: "Logging in..." })).toBeInTheDocument();
    });
});
