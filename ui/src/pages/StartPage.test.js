import { screen, render } from "@testing-library/react";
import StartPage from "./StartPage";

describe("The start page", () => {
    it("renders a button link to the home page", () => {
        render(<StartPage />);
        expect(screen.getByText("Start now")).toHaveAttribute("href", "/home");
    });
});
