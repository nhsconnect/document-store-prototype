import AuthCallbackRouter from "./AuthCallbackRouter";
import { render, screen } from "@testing-library/react";
import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";

jest.mock("../../providers/ConfigurationProvider");

describe("<AuthCallbackRouter />", () => {
    const oldWindowLocation = window.location;

    afterAll(() => {
        window.location = oldWindowLocation;
    });

    it("navigates to the token request handler URl", () => {
        const codeAndStateQueryParams = "code=some-auth-code&state=some-state";
        const allQueryParams = `?${codeAndStateQueryParams}&client_id=some-client-id`;
        const baseAPIUrl = "https://api.url";
        const tokenRequestHandlerUrl = `${baseAPIUrl}/Auth/TokenRequest?${codeAndStateQueryParams}`;
        const windowLocationProperties = {
            search: { value: allQueryParams },
            assign: { value: jest.fn() },
        };

        delete window.location;
        window.location = Object.defineProperties({}, windowLocationProperties);
        useBaseAPIUrl.mockReturnValue(baseAPIUrl);

        render(<AuthCallbackRouter />);

        expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        expect(window.location.assign).toHaveBeenCalledWith(tokenRequestHandlerUrl);
    });

    it("returns a loading state until redirection to token request handler", () => {
        render(<AuthCallbackRouter />);

        expect(screen.getByText("Loading...")).toBeInTheDocument();
    });
});
