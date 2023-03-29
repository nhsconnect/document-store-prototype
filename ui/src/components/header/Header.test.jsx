import { render, screen, within } from "@testing-library/react";
import Header from "./Header";
import routes from "../../enums/routes";
import { useFeatureToggle } from "../../providers/configProvider/ConfigProvider";
import { useAuth } from "react-oidc-context";
import { MemoryRouter } from "react-router";

jest.mock("../../providers/configProvider/ConfigProvider");
jest.mock("react-oidc-context");

describe("Header", () => {
    describe("default rendering", () => {
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

        it("renders a nav", () => {
            render(<Header />);

            expect(screen.getByRole("navigation")).toBeInTheDocument();
        });
    });

    describe("OIDC_AUTHENTICATION toggle", () => {
        afterEach(() => {
            jest.clearAllMocks();
        });

        describe("toggled on", () => {
            beforeEach(() => {
                useFeatureToggle.mockReturnValue(true);
            });

            it("renders nav links when authenticated", () => {
                useAuth.mockReturnValue({ isAuthenticated: true });

                renderHeaderWithRouter();

                const nav = screen.getByRole("navigation");
                expect(within(nav).getAllByRole("link")).toHaveLength(2);
                expect(useAuth).toHaveBeenCalled();
            });

            it("does not render nav links when unauthenticated", () => {
                useAuth.mockReturnValue({ isAuthenticated: false });

                renderHeaderWithRouter();

                const nav = screen.getByRole("navigation");
                expect(within(nav).queryByRole("link")).not.toBeInTheDocument();
                expect(useAuth).toHaveBeenCalled();
            });
        });

        describe("toggled off", () => {
            beforeEach(() => {
                useFeatureToggle.mockReturnValue(false);
            });

            afterEach(() => {
                sessionStorage.clear();
            });

            it("renders nav links when authenticated", () => {
                sessionStorage.setItem("LoggedIn", "true");

                render(<Header />);

                const nav = screen.getByRole("navigation");
                expect(within(nav).getAllByRole("link")).toHaveLength(2);
                expect(useAuth).not.toHaveBeenCalled();
            });

            it("does not render nav links when unauthenticated", () => {
                sessionStorage.setItem("LoggedIn", "false");

                render(<Header />);

                const nav = screen.getByRole("navigation");
                expect(within(nav).queryByRole("link")).not.toBeInTheDocument();
                expect(useAuth).not.toHaveBeenCalled();
            });
        });
    });
});

const renderHeaderWithRouter = () => {
    render(
        <MemoryRouter>
            <Header />
        </MemoryRouter>
    );
};
