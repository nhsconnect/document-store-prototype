import { render, screen } from "@testing-library/react";
import SpinnerButton from "./SpinnerButton";

describe("SpinnerButton", () => {
    it("displays status text for the spinner button", () => {
        const status = "Loading...";

        render(<SpinnerButton status={status} />);

        expect(screen.getByRole("SpinnerButton", { name: status })).toBeInTheDocument();
        expect(screen.getByRole("status")).toBeInTheDocument();
    });
});
