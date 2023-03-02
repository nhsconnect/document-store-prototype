import { render, screen, waitFor } from "@testing-library/react";
import DeleteDocumentsConfirmationPage from "./DeleteDocumentsConfirmationPage";
import { usePatientDetailsProviderContext } from "../../providers/PatientDetailsProvider";
import "../../apiClients/documentStore";
import userEvent from "@testing-library/user-event";
import { buildPatientDetails } from "../../utils/testBuilders";
import { useNavigate } from "react-router";
import { useDocumentStore } from "../../apiClients/documentStore";

jest.mock("react-router");
jest.mock("../../apiClients/documentStore");
jest.mock("../../providers/PatientDetailsProvider");

describe("<DeleteDocumentsConfirmationPage />", () => {
    beforeEach(() => {
        usePatientDetailsProviderContext.mockReturnValue([buildPatientDetails()]);
    });

    it("renders the page", () => {
        const nhsNumber = "9000000009";
        const givenName = ["Bill"];
        const familyName = "Jobs";
        const patientDetails = buildPatientDetails({ nhsNumber, givenName, familyName });

        usePatientDetailsProviderContext.mockReturnValue([patientDetails]);

        render(<DeleteDocumentsConfirmationPage />);

        expect(
            screen.getByRole("heading", {
                name: "Delete health records and attachments",
            })
        ).toBeInTheDocument();
        expect(
            screen.getByText(
                `Are you sure you want to permanently delete all files for patient ${givenName} ${familyName} NHS number ${nhsNumber}?`
            )
        ).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "Yes" })).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "No" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
        expect(
            screen.queryByText("There has been an issue deleting these records, please try again later.")
        ).not.toBeInTheDocument();
        expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
    });

    it("selects the no option is selected by default", () => {
        render(<DeleteDocumentsConfirmationPage />);

        expect(screen.getByRole("radio", { name: "No" })).toBeChecked();
    });

    it("navigates to /search/results when selecting no", async () => {
        const navigateMock = jest.fn();
        const deleteAllDocumentsMock = jest.fn();

        useNavigate.mockReturnValue(navigateMock);
        useDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });

        render(<DeleteDocumentsConfirmationPage />);
        userEvent.click(screen.getByRole("radio", { name: "No" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(navigateMock).toHaveBeenCalledWith("/search/results");
        });
        expect(deleteAllDocumentsMock).not.toHaveBeenCalled();
    });

    it("navigates to /search/results when selecting yes and successfully deleting docs", async () => {
        const nhsNumber = "9000000009";
        const patientDetails = buildPatientDetails({ nhsNumber });
        const navigateMock = jest.fn();
        const deleteAllDocumentsMock = jest.fn();

        useNavigate.mockReturnValue(navigateMock);
        useDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        usePatientDetailsProviderContext.mockReturnValue([patientDetails]);
        deleteAllDocumentsMock.mockResolvedValue("successfully deleted");

        render(<DeleteDocumentsConfirmationPage />);
        userEvent.click(screen.getByRole("radio", { name: "Yes" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(navigateMock).toHaveBeenCalledWith("/search/results");
        });
        expect(deleteAllDocumentsMock).toHaveBeenCalledWith(nhsNumber);
    });

    it("displays progress bar and disabled continue button whilst deleting docs", async () => {
        const deleteAllDocumentsMock = jest.fn();

        useDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        deleteAllDocumentsMock.mockResolvedValue("successfully deleted");

        render(<DeleteDocumentsConfirmationPage />);
        userEvent.click(screen.getByRole("radio", { name: "Yes" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(screen.getByRole("progressbar")).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Continue" })).toBeDisabled();
        });

        expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeEnabled();
    });

    it("does not navigate to /search/results when API call to delete docs fails", async () => {
        const deleteAllDocumentsMock = jest.fn();
        const navigateMock = jest.fn();

        useDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        useNavigate.mockReturnValue(navigateMock);
        deleteAllDocumentsMock.mockRejectedValue(new Error("Failed to delete docs"));

        render(<DeleteDocumentsConfirmationPage />);
        userEvent.click(screen.getByRole("radio", { name: "Yes" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        expect(
            await screen.findByText("There has been an issue deleting these records, please try again later.")
        ).toBeInTheDocument();
        expect(navigateMock).not.toHaveBeenCalled();
    });
});
