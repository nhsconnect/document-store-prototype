import ProtectedRoutes from "./ProtectedRoutes";
import { MemoryRouter, useNavigate } from "react-router";
import { render, screen } from "@testing-library/react";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

jest.mock("../../providers/sessionProvider/SessionProvider");
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));

describe("ProtectedRoutes", () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it.each([false, null, undefined])(
        "does not render children when user IS NOT authenticated with isLoggedIn is: %s",
        (loggedInValues) => {
            const notRenderedText = "Not Rendered Text";

            const session = {
                isLoggedIn: loggedInValues,
            };
            useSessionContext.mockReturnValue([session]);

            useNavigate.mockReturnValue(jest.fn());

            renderProtectedRoutes(<div>{notRenderedText}</div>);

            expect(screen.queryByText(notRenderedText)).not.toBeInTheDocument();
        }
    );

    it("does redirect if a user IS NOT authenticated", () => {
        const navigateMock = jest.fn();

        const session = {
            isLoggedIn: false,
        };
        useSessionContext.mockReturnValue([session]);

        useNavigate.mockReturnValue(navigateMock);

        renderProtectedRoutes(<div>this-should-NOT-be-rendered</div>);

        expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
    });

    it("does render children when user IS authenticated", () => {
        const renderedText = "Rendered Text";

        const session = {
            isLoggedIn: true,
        };
        useSessionContext.mockReturnValue([session]);

        useNavigate.mockReturnValue(jest.fn());

        renderProtectedRoutes(<div>{renderedText}</div>);

        expect(screen.getByText(renderedText)).toBeInTheDocument();
    });

    it("does not redirect if the user IS authenticated", async () => {
        const navigateMock = jest.fn();

        const session = {
            isLoggedIn: true,
        };
        useSessionContext.mockReturnValue([session]);

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
