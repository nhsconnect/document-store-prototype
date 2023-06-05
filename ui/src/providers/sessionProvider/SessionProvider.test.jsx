import SessionProvider, { useSessionContext } from "./SessionProvider";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("SessionProvider", () => {
    afterEach(() => {
        jest.clearAllMocks();
        sessionStorage.clear();
    });

    it("gets the logged in value from session storage", () => {
        renderSessionProvider(<TestComponent />);

        expect(sessionStorage.getItem).toHaveBeenCalledWith("LoggedIn");
    });

    it("sets the logged in state to false if session storage logged in value is false", () => {
        sessionStorage.setItem("LoggedIn", "false");

        renderSessionProvider(<TestComponent />);

        expect(screen.getByText("Is Not Logged In")).toBeInTheDocument();
        expect(screen.queryByText("Is Logged In")).not.toBeInTheDocument();
    });

    it("sets the logged in state to true if session storage logged in value is true", () => {
        sessionStorage.setItem("LoggedIn", "true");

        renderSessionProvider(<TestComponent />);

        expect(screen.getByText("Is Logged In")).toBeInTheDocument();
        expect(screen.queryByText("Is Not Logged In")).not.toBeInTheDocument();
    });

    it("sets the logged in session storage value to true when state changes with logged in set to true", async () => {
        sessionStorage.setItem("LoggedIn", "false");

        renderSessionProvider(<TestComponent />);
        userEvent.click(screen.getByRole("button", { name: "Log In" }));

        expect(screen.getByText("Is Logged In")).toBeInTheDocument();
        await waitFor(() => {
            expect(sessionStorage.setItem).toHaveBeenCalledWith("LoggedIn", "true");
        });
    });

    it("sets the logged in session storage value to false when state changes with logged in set to false", async () => {
        sessionStorage.setItem("LoggedIn", "true");

        renderSessionProvider(<TestComponent />);
        userEvent.click(screen.getByRole("button", { name: "Log Out" }));

        expect(screen.getByText("Is Not Logged In")).toBeInTheDocument();
        await waitFor(() => {
            expect(sessionStorage.setItem).toHaveBeenCalledWith("LoggedIn", "false");
        });
    });
});

const renderSessionProvider = (children) => {
    render(<SessionProvider>{children}</SessionProvider>);
};

const TestComponent = () => {
    const [session, setSession, deleteSession] = useSessionContext();

    const isLoggedInText = session.isLoggedIn ? "Is Logged In" : "Is Not Logged In";

    const handleLogIn = () => {
        setSession({ userRole: "USER", isLoggedIn: true });
    };

    return (
        <>
            <p>
                {isLoggedInText}
                <button onClick={handleLogIn}>Log In</button>
                <button onClick={deleteSession}>Log Out</button>
            </p>
        </>
    );
};
