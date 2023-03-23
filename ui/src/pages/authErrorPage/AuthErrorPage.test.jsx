import AuthErrorPage from "./AuthErrorPage";
import { render, screen } from "@testing-library/react";
import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";

jest.mock("../../providers/ConfigurationProvider");

describe("AuthErrorPage", () => {
    it("renders the page", () => {
        const baseApiUrl = "https://api.url";
        const loginHandlerUrl = `${baseApiUrl}/Auth/Login`;
        const helpDeskUrl = "https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks";

        useBaseAPIUrl.mockReturnValue(baseApiUrl);

        render(<AuthErrorPage />);

        expect(useBaseAPIUrl).toHaveBeenCalledWith("doc-store-api");
        expect(screen.getByRole("link", { name: "NHS National Service Desk" })).toHaveAttribute("href", helpDeskUrl);
        expect(screen.getByRole("button", { name: "Log In" })).toHaveAttribute("href", loginHandlerUrl);
        expect(screen.getByRole("heading", { name: "You have been logged out" })).toBeInTheDocument();
    });
});
