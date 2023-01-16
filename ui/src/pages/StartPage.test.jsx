import {render, screen} from "@testing-library/react";
import StartPage from "./StartPage";

describe("<StartPage/>", () => {
    it("renders the page header", () => {
        render(<StartPage/>);

        expect(screen.getByRole("heading", {name: "Document Store"})).toBeInTheDocument();
    })

    it('renders a list of service features', () => {
        render(<StartPage/>);

        expect(screen.getByText("Use this service to:")).toBeInTheDocument()
        expect(screen.getByRole("list")).toBeInTheDocument();
        expect(screen.getAllByRole("listitem")).toHaveLength(2);
    });

    it("renders a 'before you start' heading", () => {
        render(<StartPage/>);

        expect(screen.getByRole("heading", {name: "Before you start"})).toBeInTheDocument();
        expect(screen.getByText("You can only use this service if you have a valid CIS2 account")).toBeInTheDocument();
    });

    it("renders a button link with an href to /home", () => {
        render(<StartPage/>);

        expect(screen.getByRole("button", {name: "Start now"})).toHaveAttribute("href", "/home");
    });
});
