import ProtectedRoutes from "./ProtectedRoutes";
import { MemoryRouter, useNavigate } from "react-router";
import { render, screen } from "@testing-library/react";
import routes from "../../enums/routes";

jest.mock("react-cookie");
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));

describe("ProtectedRoutes", () => {
    beforeEach(() => {
        localStorage.clear();
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    it.each(["false", ""])("does not render children when user IS NOT authenticated", (loggedInValues) => {
        const notRenderedText = "Not Rendered Text";

        localStorage.setItem("LoggedIn", loggedInValues);
        useNavigate.mockReturnValue(jest.fn());

        renderProtectedRoutes(<div>{notRenderedText}</div>);

        expect(localStorage.getItem).toHaveBeenCalledWith("LoggedIn");
        expect(screen.queryByText(notRenderedText)).not.toBeInTheDocument();
    });

    it("does redirect if a user IS NOT authenticated", () => {
        const navigateMock = jest.fn();

        localStorage.setItem("LoggedIn", "false");
        useNavigate.mockReturnValue(navigateMock);

        renderProtectedRoutes(<div>this-should-NOT-be-rendered</div>);

        expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
    });

    it("does render children when user IS authenticated", () => {
        const renderedText = "Rendered Text";

        localStorage.setItem("LoggedIn", "true");

        renderProtectedRoutes(<div>{renderedText}</div>);

        expect(localStorage.getItem).toHaveBeenCalledWith("LoggedIn");
        expect(screen.getByText(renderedText)).toBeInTheDocument();
    });

    it("does not redirect if the user IS authenticated", async () => {
        const navigateMock = jest.fn();

        localStorage.setItem("LoggedIn", "true");
        useNavigate.mockReturnValue(navigateMock);

        renderProtectedRoutes(<div>this-should-be-rendered</div>);

        expect(navigateMock).not.toHaveBeenCalled();
    });
});

const renderProtectedRoutes = (children) => {
    render(
        <MemoryRouter>
            <ProtectedRoutes>{children}</ProtectedRoutes>
        </MemoryRouter>
    );
};
