import { render, screen } from "@testing-library/react";
import SimpleProgressBar from "./SimpleProgressBar";

describe("SimpleProgressBar", () => {
    it("displays the text passed to it as an argument", async () => {
        const expectedText = "Loading...";
        render(<SimpleProgressBar status={expectedText}></SimpleProgressBar>);

        expect(screen.getByLabelText(expectedText)).toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent(expectedText);
    });
});
