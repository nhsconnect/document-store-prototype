import { render, screen } from "@testing-library/react";
import StartPage from "./StartPage";
import { MemoryRouter, useNavigate } from "react-router";
import { useBaseAPIUrl, useFeatureToggle } from "../../providers/configProvider/ConfigProvider";

jest.mock("../../providers/configProvider/ConfigProvider");
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));

const renderPage = () =>
    render(
        <MemoryRouter>
            <StartPage />
        </MemoryRouter>
    );
describe("StartPage", () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it("renders the page header", () => {
        const navigateMock = jest.fn();
        useNavigate.mockReturnValue(navigateMock);

        renderPage();

        expect(screen.getByRole("heading", { name: "Inactive Patient Record Administration" })).toBeInTheDocument();
    });

    it("renders service info", () => {
        const navigateMock = jest.fn();
        useNavigate.mockReturnValue(navigateMock);

        renderPage();

        expect(screen.getByText(/When a patient is inactive/)).toBeInTheDocument();
        expect(screen.getByText(/General Practice Staff/)).toBeInTheDocument();
        expect(screen.getByText(/PCSE should use this service/)).toBeInTheDocument();
    });

    it("renders service issue guidance with a link to service desk that opens in a new tab", () => {
        const navigateMock = jest.fn();
        useNavigate.mockReturnValue(navigateMock);

        renderPage();

        expect(screen.getByText(/If there is an issue/)).toBeInTheDocument();
        const nationalServiceDeskLink = screen.getByRole("link", { name: /National Service Desk/ });
        expect(nationalServiceDeskLink).toHaveAttribute(
            "href",
            "https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks"
        );
        expect(nationalServiceDeskLink).toHaveAttribute("target", "_blank");
    });

    it("renders a 'Before you start' section", () => {
        const navigateMock = jest.fn();
        useNavigate.mockReturnValue(navigateMock);
        render(<StartPage />);

        expect(screen.getByRole("heading", { name: "Before You Start" })).toBeInTheDocument();
        expect(screen.getByText(/valid NHS smartcard/)).toBeInTheDocument();
    });

    // Todo : New tests for organisation and axios get
});
