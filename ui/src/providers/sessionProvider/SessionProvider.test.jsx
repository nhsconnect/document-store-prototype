import SessionProvider, { useSessionContext } from "./SessionProvider";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

describe("SessionProvider", () => {
    it("sets the logged in state to false by default", () => {
        renderSessionProvider(<TestComponent />);

        expect(screen.getByText("Is Not Logged In")).toBeInTheDocument();
    });

    it("sets the logged in state to true when state change is triggered with logged in set to true", () => {
        renderSessionProvider(<TestComponent />);
        userEvent.click(screen.getByRole("button", { name: "Log In" }));

        expect(screen.getByText("Is Logged In")).toBeInTheDocument();
    });
});

const renderSessionProvider = (children) => {
    render(<SessionProvider>{children}</SessionProvider>);
};

const TestComponent = () => {
    const [session, setSession] = useSessionContext();

    const isLoggedInText = session.isLoggedIn ? "Is Logged In" : "Is Not Logged In";

    const handleLogIn = () => {
        setSession({ ...session, isLoggedIn: true });
    };

    return (
        <>
            <p>
                {isLoggedInText}
                <button onClick={handleLogIn}>Log In</button>
            </p>
        </>
    );
};
