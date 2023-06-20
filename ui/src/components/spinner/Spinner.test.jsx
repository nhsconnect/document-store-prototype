import { render, screen } from "@testing-library/react";
import Spinner from "./Spinner";

describe("Spinner", () => {
    it("displays status text for the spinner", () => {
        const status = "Loading...";

        render(<Spinner status={status} />);

        expect(screen.getByRole("Spinner", { name: status })).toBeInTheDocument();
        expect(screen.getByRole("status")).toBeInTheDocument();
    });
});
