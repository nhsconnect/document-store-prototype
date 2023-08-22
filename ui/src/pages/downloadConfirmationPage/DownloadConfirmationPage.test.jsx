import { render, screen } from "@testing-library/react";
import DownloadConfirmationPage from "./DownloadConfirmationPage";
import userEvent from "@testing-library/user-event";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";

jest.mock("../../providers/configProvider/ConfigProvider");

xdescribe("DownloadConfirmationPage", () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it("renders the page header", () => {
        render(<DownloadConfirmationPage />);

        expect(screen.getByRole("heading", { name: "Inactive Patient Record Administration" })).toBeInTheDocument();
    });

    it("renders service info", () => {
        render(<DownloadConfirmationPage />);

        expect(screen.getByText(/When a patient is inactive/)).toBeInTheDocument();
        expect(screen.getByText(/General Practice Staff/)).toBeInTheDocument();
        expect(screen.getByText(/PCSE should use this service/)).toBeInTheDocument();
    });

    it("renders service issue guidance with a link to service desk that opens in a new tab", () => {
        render(<DownloadConfirmationPage />);

        expect(screen.getByText(/If there is an issue/)).toBeInTheDocument();
        const nationalServiceDeskLink = screen.getByRole("link", { name: /National Service Desk/ });
        expect(nationalServiceDeskLink).toHaveAttribute(
            "href",
            "https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks"
        );
        expect(nationalServiceDeskLink).toHaveAttribute("target", "_blank");
    });

    it("renders a 'Before you start' section", () => {
        render(<DownloadConfirmationPage />);

        expect(screen.getByRole("heading", { name: "Before You Start" })).toBeInTheDocument();
        expect(screen.getByText(/valid NHS smartcard/)).toBeInTheDocument();
    });

    it("renders a button with a valid redirect url to the auth login endpoint", () => {
        const baseUrl = "http://dummy.com";
        const apiName = "doc-store-api";
        useBaseAPIUrl.mockReturnValue(baseUrl);

        render(<DownloadConfirmationPage />);
        userEvent.click(screen.getByRole("button", { name: "Start now" }));
        expect(useBaseAPIUrl).toHaveBeenCalledWith(apiName);
    });

    it("renders a spinner when redirect button is clicked", () => {
        const mockWindow = Object.create(window);
        const baseUrl = "http://dummy.com";
        Object.defineProperty(mockWindow, "location", {
            value: {
                href: baseUrl,
            },
        });

        render(<DownloadConfirmationPage />);
        userEvent.click(screen.getByRole("button", { name: "Start now" }));

        expect(screen.getByText(/Logging in.../)).toBeInTheDocument();
    });
});
