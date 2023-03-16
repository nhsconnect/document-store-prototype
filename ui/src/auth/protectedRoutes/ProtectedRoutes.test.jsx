import ProtectedRoutes from "./ProtectedRoutes";
import { useCookies } from "react-cookie";
import { MemoryRouter, useNavigate } from "react-router";
import { render, screen } from "@testing-library/react";
import routes from "../../enums/routes";

jest.mock("react-cookie");
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));
describe("ProtectedRoutes", () => {
    it("does not render children when user IS NOT authenticated", () => {
        const cookies = {};

        useCookies.mockReturnValue([cookies]);
        useNavigate.mockReturnValue(jest.fn());

        renderProtectedRoutes(<div>this-should-NOT-be-rendered</div>);

        expect(useCookies).toHaveBeenCalledWith(["LoggedIn"]);
        expect(screen.queryByText("this-should-NOT-be-rendered")).not.toBeInTheDocument();
    });

    it("does redirect if a user IS NOT authenticated", () => {
        const navigateMock = jest.fn();
        const cookies = {};

        useCookies.mockReturnValue([cookies]);
        useNavigate.mockReturnValue(navigateMock);

        renderProtectedRoutes(<div>this-should-NOT-be-rendered</div>);

        expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
    });

    it("does render children when user IS authenticated", () => {
        const cookies = { LoggedIn: "some cookie" };

        useCookies.mockReturnValue([cookies]);

        renderProtectedRoutes(<div>this-should-be-rendered</div>);

        expect(useCookies).toHaveBeenCalledWith(["LoggedIn"]);
        expect(screen.getByText("this-should-be-rendered")).toBeInTheDocument();
    });

    it("does not redirect if the user IS authenticated", async () => {
        const navigateMock = jest.fn();
        const cookies = { LoggedIn: "some cookie" };

        useCookies.mockReturnValue([cookies]);
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
