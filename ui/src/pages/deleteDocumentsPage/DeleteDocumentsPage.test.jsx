import { render, screen, waitFor } from "@testing-library/react";
import DeleteDocumentsPage from "./DeleteDocumentsPage";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import userEvent from "@testing-library/user-event";
import { buildPatientDetails } from "../../utils/testBuilders";
import { useNavigate } from "react-router";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import routes from "../../enums/routes";

jest.mock("react-router");
jest.mock("../../providers/documentStoreProvider/DocumentStoreProvider");
jest.mock("../../providers/patientDetailsProvider/PatientDetailsProvider");

describe("DeleteDocumentsPage", () => {
    beforeEach(() => {
        usePatientDetailsContext.mockReturnValue([buildPatientDetails()]);
    });

    it("renders the page", () => {
        const nhsNumber = "9000000009";
        const givenName = ["Bill"];
        const familyName = "Jobs";
        const patientDetails = buildPatientDetails({ nhsNumber, givenName, familyName });

        usePatientDetailsContext.mockReturnValue([patientDetails]);

        render(<DeleteDocumentsPage />);

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
        render(<DeleteDocumentsPage />);

        expect(screen.getByRole("radio", { name: "No" })).toBeChecked();
    });

    it("navigates to /search/results when selecting no", async () => {
        const navigateMock = jest.fn();
        const deleteAllDocumentsMock = jest.fn();

        useNavigate.mockReturnValue(navigateMock);
        useAuthorisedDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });

        render(<DeleteDocumentsPage />);
        userEvent.click(screen.getByRole("radio", { name: "No" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(navigateMock).toHaveBeenCalledWith(routes.SEARCH_RESULTS);
        });
        expect(deleteAllDocumentsMock).not.toHaveBeenCalled();
    });

    it("navigates to /search/results when selecting yes and successfully deleting docs", async () => {
        const nhsNumber = "9000000009";
        const patientDetails = buildPatientDetails({ nhsNumber });
        const navigateMock = jest.fn();
        const deleteAllDocumentsMock = jest.fn();

        useNavigate.mockReturnValue(navigateMock);
        useAuthorisedDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        usePatientDetailsContext.mockReturnValue([patientDetails]);
        deleteAllDocumentsMock.mockResolvedValue("successfully deleted");

        render(<DeleteDocumentsPage />);
        userEvent.click(screen.getByRole("radio", { name: "Yes" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(navigateMock).toHaveBeenCalledWith(routes.SEARCH_RESULTS);
        });
        expect(deleteAllDocumentsMock).toHaveBeenCalledWith(nhsNumber);
    });

    it("displays progress bar and disabled continue button whilst deleting docs", async () => {
        const deleteAllDocumentsMock = jest.fn();

        useAuthorisedDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        deleteAllDocumentsMock.mockResolvedValue("successfully deleted");

        render(<DeleteDocumentsPage />);
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

        useAuthorisedDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        deleteAllDocumentsMock.mockRejectedValue(new Error("Failed to delete docs"));

        render(<DeleteDocumentsPage />);
        await waitFor(() => {
            userEvent.click(screen.getByRole("radio", { name: "Yes" }));
            userEvent.click(screen.getByRole("button", { name: "Continue" }));
        });

        expect(await screen.queryByText("Download electronic health records and attachments")).not.toBeInTheDocument();
    });

    it("navigates to the start page when API call to delete docs is made without a valid backend session", async () => {
        const errorResponse = {
            response: {
                status: 403,
                message: "Unauthorised",
            },
        };

        const deleteAllDocumentsMock = jest.fn();
        const navigateMock = jest.fn();

        useNavigate.mockReturnValue(navigateMock);
        useAuthorisedDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        deleteAllDocumentsMock.mockRejectedValue(errorResponse);

        render(<DeleteDocumentsPage />);
        userEvent.click(screen.getByRole("radio", { name: "Yes" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
        });
    });

    it("does not navigate to /search/results when API call to delete docs fails when selecting no", async () => {
        const deleteAllDocumentsMock = jest.fn();

        useAuthorisedDocumentStore.mockReturnValue({ deleteAllDocuments: deleteAllDocumentsMock });
        deleteAllDocumentsMock.mockRejectedValue(new Error("Failed to delete docs"));

        render(<DeleteDocumentsPage />);
        await waitFor(() => {
            userEvent.click(screen.getByRole("radio", { name: "No" }));
            userEvent.click(screen.getByRole("button", { name: "Continue" }));
        });

        expect(await screen.queryByText("Download electronic health records and attachments")).not.toBeInTheDocument();
    });
});
