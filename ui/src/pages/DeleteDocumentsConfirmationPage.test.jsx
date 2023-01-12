import {render, screen, waitFor} from "@testing-library/react";
import DeleteDocumentsConfirmationPage from "./DeleteDocumentsConfirmationPage";
import {useNhsNumberProviderContext} from "../providers/NhsNumberProvider";
import useApi from "../apiClients/useApi";
import userEvent from "@testing-library/user-event";
import {useDeleteDocumentsResponseProviderContext} from "../providers/DeleteDocumentsResponseProvider";

const mockNavigate = jest.fn();
jest.mock("../apiClients/useApi");

jest.mock("../apiClients/useApi");
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: jest.fn(),
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
const patientDetailsResponse = {
    result: {
        patientDetails: patientData,
    },
};

describe("<DeleteDocumentsConfirmationPage />", () => {
    it("renders the page", async () => {
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: () => {
                    return patientDetailsResponse;
                },
            };
        });
        useNhsNumberProviderContext.mockReturnValue([fakeNhsNumber, jest.fn()]);
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
                    `Are you sure you want to permanently delete all files for patient ${patientDetailsResponse.result.patientDetails.familyName} ${patientDetailsResponse.result.patientDetails.givenName[0]} NHS number ${patientDetailsResponse.result.patientDetails.nhsNumber} ?`
                )
            ).toBeInTheDocument();
        });
        expect(screen.getByRole("radio", { name: "Yes" })).toBeInTheDocument();
        expect(screen.getByRole("radio", { name: "No" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
    });
    it('should navigate to SearchResultsPage when user choose radio button "NO" and click on continue button', async () => {
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: () => {
                    return patientDetailsResponse;
                },
            };
        });
        useNhsNumberProviderContext.mockReturnValue([fakeNhsNumber, jest.fn()]);
        useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);
        render(<DeleteDocumentsConfirmationPage />);
        expect(screen.getByRole("radio", { name: "No" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
        userEvent.click(screen.getByRole("button", { name: "Continue" }));
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith("/search/results");
        });
    });
describe("when user choose YES radio button and clicks continue", ()=> {
    it('should navigate to search results page with delete document response as "successful" when deleteAllDocuments api method returns successfully deleted message ', async () => {
        const deleteDocumentsResponse = "successful";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: () => patientDetailsResponse,
                deleteAllDocuments: () => "successfully deleted",
            };
        });
        useNhsNumberProviderContext.mockReturnValue([fakeNhsNumber, jest.fn()]);
        useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);
        render(<DeleteDocumentsConfirmationPage/>);
        expect(screen.getByRole("radio", {name: "Yes"})).toBeInTheDocument();
        userEvent.click(screen.getByRole("radio", {name: "Yes"}));
        expect(screen.getByRole("button", {name: "Continue"})).toBeInTheDocument();
        userEvent.click(screen.getByRole("button", {name: "Continue"}));
        await waitFor(() => {
            expect(useApi().deleteAllDocuments()).toBe("successfully deleted");
        });
        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith("/search/results");
        });
    });
    it('should navigate to search results page with delete documents response as "unsuccessful" when api call throws an error ', async () => {
        const deleteDocumentsResponse = "unsuccessful";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: () => patientDetailsResponse,
                deleteAllDocuments: () => {throw new Error();}
            };
        });
        useNhsNumberProviderContext.mockReturnValue([fakeNhsNumber, jest.fn()]);
        useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);
        render(<DeleteDocumentsConfirmationPage/>);
        expect(screen.getByRole("radio", {name: "Yes"})).toBeInTheDocument();
        userEvent.click(screen.getByRole("radio", {name: "Yes"}));
        expect(screen.getByRole("button", {name: "Continue"})).toBeInTheDocument();
        await waitFor(()=>{
            userEvent.click(screen.getByRole("button", {name: "Continue"}));
        })
        expect(() =>{useApi().deleteAllDocuments()}).toThrow();

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith("/search/results");
        });
    });
});

it("should navigate to search results page when user choose No and clicks continue", async () => {
    useApi.mockImplementation(() => {
        return {
            getPatientDetails: () => patientDetailsResponse
        };
    });
     useNhsNumberProviderContext.mockReturnValue([fakeNhsNumber, jest.fn()]);
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
