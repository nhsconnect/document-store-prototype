import { render, screen } from "@testing-library/react";
import SimpleProgressBar from "./SimpleProgressBar";

describe("<SimpleProgressBar />", () => {
    it("displays status text for the progress bar", () => {
        const status = "Loading...";

        render(<SimpleProgressBar status={status} />);

        expect(screen.getByRole("progressbar", { name: status })).toBeInTheDocument();
        expect(screen.getByRole("status")).toBeInTheDocument();
    });
});
