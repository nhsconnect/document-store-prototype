import { render, screen } from "@testing-library/react";
import ServiceError from "./ServiceError";

describe("ServiceError", () => {
    it("should render the component", () => {
        render(<ServiceError />);

        expect(screen.getByText("Sorry, there is a problem with the service")).toBeInTheDocument();
    });
});
