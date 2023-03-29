import { render } from "@testing-library/react";
import { MemoryRouter, useNavigate } from "react-router";
import AuthSuccessRouter from "./AuthSuccessRouter";
import routes from "../../enums/routes";

jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));

describe("AuthSuccessRouter", () => {
    beforeEach(() => {
        sessionStorage.clear();
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    it("sets the LoggedIn session storage value to true", () => {
        useNavigate.mockReturnValue(jest.fn());

        renderAuthSuccessRouter();

        expect(sessionStorage.setItem).toHaveBeenCalledWith("LoggedIn", "true");
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
