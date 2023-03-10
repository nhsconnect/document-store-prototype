import { render, screen } from "@testing-library/react";
import LogOutLink from "./LogOutLink";
import { useBaseAPIUrl } from "../../../providers/ConfigurationProvider";
import routes from "../../../enums/routes";
import { MemoryRouter } from "react-router";

jest.mock("../../../providers/ConfigurationProvider");

describe("LogOutLink", () => {
    const oldWindowLocation = window.location;

    afterAll(() => {
        window.location = oldWindowLocation;
    });

    it("renders a log out link with an href of the LogoutHandler", () => {
        const baseApiUrl = "https://api.url";
        const baseUiUrl = "http://localhost:3000";
        const logoutHandlerUrl = `${baseApiUrl}/Auth/Logout?redirect_uri=${baseUiUrl}${routes.ROOT}`;
        const windowLocationProperties = {
            href: { value: baseUiUrl },
        };

        delete window.location;
        window.location = Object.defineProperties({}, windowLocationProperties);
        useBaseAPIUrl.mockReturnValue(baseApiUrl);

        render(
            <MemoryRouter>
                <LogOutLink />
            </MemoryRouter>
        );

        expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        expect(screen.getByRole("link", { name: "Log Out" })).toHaveAttribute("href", logoutHandlerUrl);
    });
});
