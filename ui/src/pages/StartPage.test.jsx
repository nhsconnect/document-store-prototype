import {render, screen} from "@testing-library/react";
import StartPage from "./StartPage";

describe("<StartPage/>", () => {
    it("renders the page header", () => {
        render(<StartPage />);

        expect(screen.getByRole("heading", { name: "Inactive Patient Record Administration" })).toBeInTheDocument();
    });

    it("renders info about the service", () => {
        render(<StartPage />);

        expect(screen.getByText(/When a patient is inactive/)).toBeInTheDocument();
        expect(screen.getByText(/General Practice Staff/)).toBeInTheDocument();
        expect(screen.getByText(/PCSE should use this service/)).toBeInTheDocument();
    });

    it("renders a 'before you start' heading", () => {
        render(<StartPage />);

        expect(screen.getByRole("heading", { name: "Before you start" })).toBeInTheDocument();
        expect(screen.getByText("You can only use this service if you have a valid CIS2 account")).toBeInTheDocument();
    });

    it("renders a button link with an href to /home", () => {
        render(<StartPage />);

        expect(screen.getByRole("button", { name: "Start now" })).toHaveAttribute("href", "/home");
    });
});
