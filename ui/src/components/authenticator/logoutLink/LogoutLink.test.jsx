import { render, screen } from "@testing-library/react";
import LogoutLink from "./LogoutLink";
import { useBaseAPIUrl } from "../../../providers/ConfigurationProvider";
import routes from "../../../enums/routes";
import { useCookies } from "react-cookie";

jest.mock("../../../providers/ConfigurationProvider");
jest.mock("react-cookie");

describe("LogoutLink", () => {
    const oldWindowLocation = window.location;

    afterEach(() => {
        window.location = oldWindowLocation;
    });

    it("renders a logout link with an href of the LogoutHandler", () => {
        const baseApiUrl = "https://api.url";
        const baseUiUrl = "http://localhost:3000";
        const logoutHandlerUrl = `${baseApiUrl}/Auth/Logout?redirect_uri=${baseUiUrl}${routes.ROOT}`;
        const cookies = { LoggedIn: "some cookie" };
        const windowLocationProperties = {
            href: { value: baseUiUrl },
        };

        delete window.location;
        window.location = Object.defineProperties({}, windowLocationProperties);

        useBaseAPIUrl.mockReturnValue(baseApiUrl);
        useCookies.mockReturnValue([cookies]);

        render(<LogoutLink />);

        expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        expect(screen.getByRole("link", { name: "Log Out" })).toHaveAttribute("href", logoutHandlerUrl);
    });

    it("does not render the logout link if SessionId cookie is not present", () => {
        const cookies = {};

        useCookies.mockReturnValue([cookies]);

        render(<LogoutLink />);

        expect(useCookies).toHaveBeenCalledWith(["LoggedIn"]);
        expect(screen.queryByRole("link", { name: "Log Out" })).not.toBeInTheDocument();
    });
});
