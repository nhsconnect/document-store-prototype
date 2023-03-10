import { render, screen, within } from "@testing-library/react";
import HeaderContainer from "./HeaderContainer";
import routes from "../../enums/routes";
import { useFeatureToggle } from "../../providers/ConfigurationProvider";
import { useAuth } from "react-oidc-context";
import { MemoryRouter } from "react-router";

jest.mock("../../providers/ConfigurationProvider");
jest.mock("react-oidc-context");

describe("HeaderContainer", () => {
    it("renders the header", () => {
        render(<HeaderContainer />);

        expect(screen.getByRole("banner")).toBeInTheDocument();
    });

    it("renders a logo that links to the root path", () => {
        render(<HeaderContainer />);

        const nhsLogoLink = screen.getByRole("link", { name: "NHS homepage" });
        expect(nhsLogoLink).toHaveAttribute("href", routes.ROOT);
        expect(within(nhsLogoLink).getByRole("img", { name: "NHS Logo" })).toBeInTheDocument();
    });

    it("renders a heading that links to the root path", () => {
        render(<HeaderContainer />);

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
                <HeaderContainer />
            </MemoryRouter>
        );

        expect(screen.getByRole("link", { name: "Log Out" })).toBeInTheDocument();
        expect(useAuth).toHaveBeenCalled();
    });

    it("renders the session log out link when OIDC_AUTHENTICATION is toggled off", () => {
        useFeatureToggle.mockReturnValue(false);

        render(<HeaderContainer />);

        expect(screen.getByRole("link", { name: "Log Out" })).toBeInTheDocument();
        expect(useAuth).not.toHaveBeenCalled();
    });
});
