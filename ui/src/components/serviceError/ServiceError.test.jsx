import { render, screen } from "@testing-library/react";
import ServiceError from "./ServiceError";

describe("ServiceError", () => {
    it("should render the component", () => {
        render(<ServiceError />);

        expect(screen.getByText("Sorry, the service is currently unavailable.")).toBeInTheDocument();
        expect(screen.getByText("Please try again later.")).toBeInTheDocument();
    });

    it("should display the error message if there is one", () => {
        const message = "Error message";
        render(<ServiceError message={message} />);

        expect(screen.getByText("Sorry, the service is currently unavailable.")).toBeInTheDocument();
        expect(screen.getByText(message)).toBeInTheDocument();
    });
});
