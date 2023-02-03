import { render, screen, waitFor } from "@testing-library/react";
import DeleteDocumentsConfirmationPage from "./DeleteDocumentsConfirmationPage";
import { usePatientDetailsProviderContext } from "../providers/PatientDetailsProvider";
import "../apiClients/documentStore";
import userEvent from "@testing-library/user-event";
import { useDeleteDocumentsResponseProviderContext } from "../providers/DeleteDocumentsResponseProvider";

const mockNavigate = jest.fn();

const mockDocumentStore = {
    deleteAllDocuments: () => "Test message",
};
jest.mock("../apiClients/documentStore", () => {
    return {
        useDocumentStore: () => mockDocumentStore,
    };
});

jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));
jest.mock("../providers/PatientDetailsProvider", () => ({
    usePatientDetailsProviderContext: jest.fn(),
}));
jest.mock("../providers/DeleteDocumentsResponseProvider", () => ({
    useDeleteDocumentsResponseProviderContext: jest.fn(),
}));
const fakeNhsNumber = "9000000009";
const deleteDocumentsResponse = "";
const patientData = {
    birthDate: "2010-10-22",
    familyName: "Doe",
    givenName: ["Jane"],
    nhsNumber: fakeNhsNumber,
    postalCode: "LS1 6AE",
};

describe("<DeleteDocumentsConfirmationPage />", () => {
    it("renders the page", async () => {
        usePatientDetailsProviderContext.mockReturnValue([patientData, jest.fn()]);
        useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);
        render(<DeleteDocumentsConfirmationPage />);
        expect(
            screen.getByRole("heading", {
                name: "Delete health records and attachments",
            })
        ).toBeInTheDocument();
        await waitFor(() => {
            expect(
                screen.getByText(
                    `Are you sure you want to permanently delete all files for patient ${patientData.givenName.join(
                        " "
                    )} ${patientData.familyName} NHS number ${patientData.nhsNumber} ?`
                )
            ).toBeInTheDocument();
        });
        expect(screen.getByRole("radio", { name: "Yes" })).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "No" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
    });

    it('should navigate to SearchResultsPage when user choose radio button "NO" and click on continue button', async () => {
        usePatientDetailsProviderContext.mockReturnValue([patientData, jest.fn()]);
        useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);
        render(<DeleteDocumentsConfirmationPage />);
        expect(screen.getByRole("radio", { name: "No" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
        userEvent.click(screen.getByRole("button", { name: "Continue" }));
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith("/search/results");
        });
    });

    describe("when user choose YES radio button and clicks continue", () => {
        it('should navigate to search results page with delete document response as "successful" when deleteAllDocuments api method returns successfully deleted message ', async () => {
            const deleteDocumentsResponse = "successful";
            (mockDocumentStore.deleteAllDocuments = () => "successfully deleted"),
                usePatientDetailsProviderContext.mockReturnValue([patientData, jest.fn()]);
            useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);
            render(<DeleteDocumentsConfirmationPage />);
            expect(screen.getByRole("radio", { name: "Yes" })).toBeInTheDocument();
            userEvent.click(screen.getByRole("radio", { name: "Yes" }));
            expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
            userEvent.click(screen.getByRole("button", { name: "Continue" }));
            await waitFor(() => {
                expect(screen.getByRole("progressbar")).toBeInTheDocument();
                expect(screen.getByRole("button", { name: "Continue" })).toBeDisabled();
            });

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith("/search/results");
            });
        });

        it("should not navigate to search results page when api call throws an error ", async () => {
            mockDocumentStore.deleteAllDocuments = () => {
                throw new Error();
            };

            usePatientDetailsProviderContext.mockReturnValue([patientData, jest.fn()]);
            render(<DeleteDocumentsConfirmationPage />);
            expect(screen.getByRole("radio", { name: "Yes" })).toBeInTheDocument();
            userEvent.click(screen.getByRole("radio", { name: "Yes" }));

            expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();

            userEvent.click(screen.getByRole("button", { name: "Continue" }));

            await waitFor(() => {
                expect(
                    screen.getByText("There has been an issue deleting these records, please try again later.")
                ).toBeInTheDocument();
                expect(mockNavigate).not.toHaveBeenCalled();
            });
        });
    });

    it("should navigate to search results page when user choose No and clicks continue", async () => {
        usePatientDetailsProviderContext.mockReturnValue([patientData, jest.fn()]);
        useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);

        render(<DeleteDocumentsConfirmationPage />);
        expect(screen.getByRole("radio", { name: "No" })).toBeInTheDocument();
        userEvent.click(screen.getByRole("radio", { name: "No" }));
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
        userEvent.click(screen.getByRole("button", { name: "Continue" }));
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith("/search/results");
        });
    });
});
