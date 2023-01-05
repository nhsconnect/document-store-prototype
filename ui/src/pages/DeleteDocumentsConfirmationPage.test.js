import {render, screen, waitFor} from "@testing-library/react";
import DeleteDocumentsConfirmationPage from "./DeleteDocumentsConfirmationPage";
import {useNhsNumberProviderContext} from "../providers/NhsNumberProvider";
import useApi from "../apiClients/useApi";


const mockNavigate = jest.fn();
jest.mock("../apiClients/useApi");
jest.mock('react-router', () => ({
    useNavigate: () => mockNavigate,
}));
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: jest.fn(),
}));
const fakeNhsNumber = "9000000009";
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

describe('<DeleteDocumentsConfirmationPage />', ()=>{
    it('renders the page', async () => {
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: () => {
                    return patientDetailsResponse;
                },
            };
        });
        useNhsNumberProviderContext.mockReturnValue([fakeNhsNumber, jest.fn()]);
        render(<DeleteDocumentsConfirmationPage/>)
        expect(screen.getByRole('heading', {name: 'Delete health records and attachments'})).toBeInTheDocument();
        await waitFor
        (()=> {
            expect(screen.getByText(`Are you sure you want to permanently delete all files for patient ${patientDetailsResponse.result.patientDetails.familyName} ${patientDetailsResponse.result.patientDetails.givenName[0]} NHS number ${patientDetailsResponse.result.patientDetails.nhsNumber} ?`)).toBeInTheDocument()
        });
        expect(screen.getByRole('radio', {name: 'Yes'})).toBeInTheDocument();
        expect(screen.getByRole('radio', {name: 'No'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Continue'})).toBeInTheDocument();
    })
})