import {render, screen} from "@testing-library/react";
import StartPage from "./StartPage";

describe("<StartPage/>", () => {
    it("renders a button link with an href to /home", () => {
        render(<StartPage/>);

        expect(screen.getByRole("button", {name: "Start now"})).toHaveAttribute("href", "/home");
    });
});
