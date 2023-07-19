import { render, screen } from "@testing-library/react";
import StartPage from "./StartPage";

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

    // it("renders a button with a href to the auth login endpoint it is clicked", () => {
    //     const baseAPIUrl = "https://api.url";

    //     useFeatureToggle.mockReturnValue(false);
    //     useBaseAPIUrl.mockReturnValue(baseAPIUrl);

    //     render(<StartPage />);

    //     //Click button

    //     /*
    //     window = Object.create(window);
    //     const url = "http://dummy.com";
    //     Object.defineProperty(window, 'location', {
    //     value: {
    //         href: url
    //     },
    //     writable: true // possibility to override
    //     });
    //     expect(window.location.href).toEqual(url);
    //     */

    //     expect(screen.getByRole("button", { name: "Start now" })).toHaveAttribute("href", `${baseAPIUrl}/Auth/Login`);
    // });
});
