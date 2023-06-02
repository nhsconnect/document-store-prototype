import { render } from "@testing-library/react";
import { MemoryRouter, useNavigate } from "react-router";
import AuthSuccessRouter from "./AuthSuccessRouter";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import UserRoles from "../../enums/userRoles";

jest.mock("../../providers/sessionProvider/SessionProvider");
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));

describe("AuthSuccessRouter", () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it("sets the LoggedIn session storage value to true", () => {
        const session = { isLoggedIn: true };
        const setSessionMock = jest.fn();

        useSessionContext.mockReturnValue([session, setSessionMock]);
        useNavigate.mockReturnValue(jest.fn());

        renderAuthSuccessRouter();

        expect(setSessionMock).toHaveBeenCalledWith({
            userRoles: UserRoles.user,
            isLoggedIn: true,
        });
    });

    it("navigates to HOME", () => {
        const navigateMock = jest.fn();

        useNavigate.mockReturnValue(navigateMock);

        renderAuthSuccessRouter();

        expect(navigateMock).toHaveBeenCalledWith(routes.HOME);
    });
});

const renderAuthSuccessRouter = () => {
    render(
        <MemoryRouter>
            <AuthSuccessRouter />
        </MemoryRouter>
    );
};
