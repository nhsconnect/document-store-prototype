import { render, screen } from "@testing-library/react";
import StartPage from "./StartPage";
import { useBaseAPIUrl, useFeatureToggle } from "../../providers/configProvider/ConfigProvider";

jest.mock("../../providers/configProvider/ConfigProvider");

describe("StartPage", () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it("renders the page header", () => {
        render(<StartPage />);

        expect(screen.getByRole("heading", { name: "Inactive Patient Record Administration" })).toBeInTheDocument();
    });

    it("renders service info", () => {
        render(<StartPage />);

        expect(screen.getByText(/When a patient is inactive/)).toBeInTheDocument();
        expect(screen.getByText(/General Practice Staff/)).toBeInTheDocument();
        expect(screen.getByText(/PCSE should use this service/)).toBeInTheDocument();
    });

    it("renders service issue guidance with a link to service desk that opens in a new tab", () => {
        render(<StartPage />);

        expect(screen.getByText(/If there is an issue/)).toBeInTheDocument();
        const nationalServiceDeskLink = screen.getByRole("link", { name: /National Service Desk/ });
        expect(nationalServiceDeskLink).toHaveAttribute(
            "href",
            "https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks"
        );
        expect(nationalServiceDeskLink).toHaveAttribute("target", "_blank");
    });

    it("renders a 'Before you start' section", () => {
        render(<StartPage />);

        expect(screen.getByRole("heading", { name: "Before You Start" })).toBeInTheDocument();
        expect(screen.getByText(/valid NHS smartcard/)).toBeInTheDocument();
    });

    it("renders a button link with an href to /home when OIDC federation is enabled", () => {
        useFeatureToggle.mockReturnValue(true);
        useFeatureToggle.mockReturnValue("https://api.url");

        render(<StartPage />);

        expect(screen.getByRole("button", { name: "Start now" })).toHaveAttribute("href", "/home");
    });

    it("renders a button link with an href to the auth login endpoint when OIDC federation is disabled", () => {
        const baseAPIUrl = "https://api.url";

        useFeatureToggle.mockReturnValue(false);
        useBaseAPIUrl.mockReturnValue(baseAPIUrl);

        render(<StartPage />);

        expect(screen.getByRole("button", { name: "Start now" })).toHaveAttribute("href", `${baseAPIUrl}/Auth/Login`);
    });
});
