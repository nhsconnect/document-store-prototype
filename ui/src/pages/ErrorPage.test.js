import { render, screen } from "@testing-library/react";
import ErrorPage from "./ErrorPage";

describe("ErrorPage", () => {
    it("should render the page", () => {
        render(<ErrorPage />);

        expect(screen.getByText("Sorry, there is a problem with the service")).toBeInTheDocument();
    });
});
