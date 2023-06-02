import { render, screen } from "@testing-library/react";
import NavLinks from "./NavLinks";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import routes from "../../enums/routes";
import userEvent from "@testing-library/user-event";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import UserRoles from "../../enums/userRoles";

jest.mock("../../providers/sessionProvider/SessionProvider");
jest.mock("../../providers/configProvider/ConfigProvider");

describe("NavLinks", () => {
    const oldWindowLocation = window.location;

    afterEach(() => {
        jest.clearAllMocks();
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

        useSessionContext.mockReturnValue([{ isLoggedIn: true }, jest.fn()]);
        useBaseAPIUrl.mockReturnValue(baseApiUrl);

        render(<NavLinks />);

        expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        expect(screen.getByRole("link", { name: "Home" })).toHaveAttribute("href", routes.ROOT);
        expect(screen.getByRole("link", { name: "Log Out" })).toHaveAttribute("href", logoutHandlerUrl);
    });

    it.each([false, null, undefined])("does not render the nav links if isLoggedIn is: %s", (loggedInValue) => {
        const session = {
            isLoggedIn: loggedInValue,
            role: UserRoles.user,
        };

        useSessionContext.mockReturnValue([session, jest.fn()]);

        render(<NavLinks />);

        expect(screen.queryByRole("link", { name: "Home" })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: "Log Out" })).not.toBeInTheDocument();
    });

    it("does not render the nav links if isLoggedIn value does not exist", () => {
        render(<NavLinks />);

        expect(screen.queryByRole("link", { name: "Home" })).not.toBeInTheDocument();
        expect(screen.queryByRole("link", { name: "Log Out" })).not.toBeInTheDocument();
    });

    it("change isLoggedIn in session context to false when logging out", () => {
        const session = {
            isLoggedIn: true,
            role: UserRoles.user,
        };
        const setSessionMock = jest.fn();
        const deleteSessionMock = jest.fn();

        useSessionContext.mockReturnValue([session, setSessionMock, deleteSessionMock]);

        render(<NavLinks />);

        userEvent.click(screen.getByRole("link", { name: "Log Out" }));

        expect(deleteSessionMock).toHaveBeenCalled();
    });
});
