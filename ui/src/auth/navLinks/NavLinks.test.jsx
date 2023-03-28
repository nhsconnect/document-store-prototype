import { render, screen } from "@testing-library/react";
import NavLinks from "./NavLinks";
import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";
import routes from "../../enums/routes";

jest.mock("../../providers/ConfigurationProvider");

describe("NavLinks", () => {
    const oldWindowLocation = window.location;

    afterEach(() => {
        jest.clearAllMocks();
        localStorage.clear();
        window.location = oldWindowLocation;
    });

    it("renders nav links with an href of the LogoutHandler", () => {
        const baseApiUrl = "https://api.url";
        const baseUiUrl = "http://localhost:3000";
        const logoutHandlerUrl = `${baseApiUrl}/Auth/Logout?redirect_uri=${baseUiUrl}${routes.ROOT}`;
        const windowLocationProperties = {
            href: { value: baseUiUrl },
        };

        delete window.location;
        window.location = Object.defineProperties({}, windowLocationProperties);

        localStorage.setItem("LoggedIn", "true");

        useBaseAPIUrl.mockReturnValue(baseApiUrl);

        render(<NavLinks />);

        expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        expect(localStorage.getItem).toHaveBeenCalledWith("LoggedIn");
        expect(screen.getByRole("link", { name: "Home" })).toHaveAttribute("href", routes.HOME);
        expect(screen.getByRole("link", { name: "Log Out" })).toHaveAttribute("href", logoutHandlerUrl);
    });

    it.each(["false", ""])("does not render the nav links if LoggedIn is: %s", (loggedInValue) => {
        localStorage.setItem("LoggedIn", loggedInValue);

        render(<NavLinks />);

        expect(localStorage.getItem).toHaveBeenCalledWith("LoggedIn");
        expect(screen.queryByRole("link", { name: "Home" })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: "Log Out" })).not.toBeInTheDocument();
    });

    it("does not render the nav links if LoggedIn value does not exist", () => {
        localStorage.clear();

        render(<NavLinks />);

        expect(screen.queryByRole("link", { name: "Home" })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: "Log Out" })).not.toBeInTheDocument();
    });
});
