import { render, screen } from "@testing-library/react";
import ProgressBar from "./ProgressBar";

describe("ProgressBar", () => {
    it("displays status text for the progress bar", () => {
        const status = "Loading...";

        render(<ProgressBar status={status} />);

        expect(screen.getByRole("progressbar", { name: status })).toBeInTheDocument();
        expect(screen.getByRole("status")).toBeInTheDocument();
    });
});
