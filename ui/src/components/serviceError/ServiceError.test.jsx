import { render, screen } from "@testing-library/react";
import ServiceError from "./ServiceError";

describe("<ServiceError />", () => {
    it("renders the service error", () => {
        render(<ServiceError />);

        expect(screen.getByRole("alert", { name: "Sorry, the service is currently unavailable." })).toBeInTheDocument();
        expect(
            screen.getByRole("heading", { name: "Sorry, the service is currently unavailable." })
        ).toBeInTheDocument();
        expect(screen.getByText("Please try again later.")).toBeInTheDocument();
        expect(
            screen.getByText(/Please check your internet connection. If the issue persists please contact the/)
        ).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "NHS Digital National Service Desk" })).toBeInTheDocument();
    });

    it("renders a link to the NHS Digital National Service Desk", () => {
        render(<ServiceError />);

        expect(screen.getByRole("link", { name: "NHS Digital National Service Desk" })).toHaveAttribute(
            "href",
            "https://digital.nhs.uk/about-nhs-digital/contact-us"
        );
    });

    it("displays the error message if there is one", () => {
        const message = "Error message";

        render(<ServiceError message={message} />);

        expect(screen.getByText(message)).toBeInTheDocument();
        expect(screen.queryByText("Please try again later.")).not.toBeInTheDocument();
    });
});
