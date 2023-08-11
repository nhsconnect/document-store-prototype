import NoValidOrgsPage from "./NoValidOrgsPage";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router";

describe("NoValidOrgsPage", () => {
    it("renders the page", () => {
        const helpDeskUrl = "https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks";

        renderNoValidOrgsPage();

        expect(screen.getByRole("link", { name: "NHS National Service Desk" })).toHaveAttribute("href", helpDeskUrl);
        expect(screen.getByRole("button", { name: "Return to start page" })).toHaveAttribute("href", "/");
        expect(
            screen.getByRole("heading", { name: "You do not have a valid organisation to access this service" })
        ).toBeInTheDocument();
    });
});

const renderNoValidOrgsPage = () => {
    render(
        <MemoryRouter>
            <NoValidOrgsPage />
        </MemoryRouter>
    );
};
