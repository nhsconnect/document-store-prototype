import { render, screen, within } from "@testing-library/react";
import Header from "./Header";
import routes from "../../enums/routes";
import { useFeatureToggle } from "../../providers/ConfigurationProvider";
import { useAuth } from "react-oidc-context";
import { MemoryRouter } from "react-router";
import { Cookies } from "react-cookie";

jest.mock("../../providers/ConfigurationProvider");
jest.mock("react-oidc-context");

describe("Header", () => {
    it("renders the header", () => {
        render(<Header />);

        expect(screen.getByRole("banner")).toBeInTheDocument();
    });

    it("renders a logo that links to the root path", () => {
        render(<Header />);

        const nhsLogoLink = screen.getByRole("link", { name: "NHS homepage" });
        expect(nhsLogoLink).toHaveAttribute("href", routes.ROOT);
        expect(within(nhsLogoLink).getByRole("img", { name: "NHS Logo" })).toBeInTheDocument();
    });

    it("renders a heading that links to the root path", () => {
        render(<Header />);

        expect(screen.getByRole("link", { name: "Inactive Patient Record Administration" })).toHaveAttribute(
            "href",
            routes.ROOT
        );
    });

    it("renders the OIDC log out link when OIDC_AUTHENTICATION is toggled on", () => {
        useFeatureToggle.mockReturnValue(true);
        useAuth.mockImplementationOnce(() => ({
            isAuthenticated: true,
            removeUser: jest.fn(),
        }));

        render(
            <MemoryRouter>
                <Header />
            </MemoryRouter>
        );

        expect(screen.getByRole("link", { name: "Log Out" })).toBeInTheDocument();
        expect(useAuth).toHaveBeenCalled();
    });

    it("renders the session log out link when OIDC_AUTHENTICATION is toggled off", () => {
        const cookies = new Cookies();
        cookies.set("LoggedIn", "some-cookie;");

        useFeatureToggle.mockReturnValue(false);

        render(<Header />);

        expect(screen.getByRole("link", { name: "Log Out" })).toBeInTheDocument();
        expect(useAuth).not.toHaveBeenCalled();
    });
});
