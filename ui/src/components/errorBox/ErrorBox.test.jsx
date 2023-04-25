import { render, screen } from "@testing-library/react";
import ErrorBox from "./ErrorBox";

describe("ErrorBox", () => {
    it("renders the error box", () => {
        render(
            <ErrorBox
                errorBoxSummaryId={"test"}
                messageTitle={"There is a problem"}
                messageBody={"Invalid NHS number"}
            />
        );

        expect(screen.getByRole("alert", { name: "There is a problem" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: "There is a problem" })).toBeInTheDocument();
        expect(screen.getByText("Invalid NHS number")).toBeInTheDocument();
    });
});
