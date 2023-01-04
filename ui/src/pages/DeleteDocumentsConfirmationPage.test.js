import { render, screen, waitFor, within } from "@testing-library/react";
import DeleteDocumentsConfirmationPage from "./DeleteDocumentsConfirmationPage";
import {useNhsNumberProviderContext} from "../providers/NhsNumberProvider";


const mockNavigate = jest.fn();
jest.mock('react-router', () => ({
    useNavigate: () => mockNavigate,
}));
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: jest.fn(),
}));

describe('<DeleteDocumentsConfirmationPage />', ()=>{
    const nhsNumber = "90000000009";
    it('renders the page', ()=>{
        useNhsNumberProviderContext.mockReturnValue([nhsNumber, jest.fn()]);
        render(<DeleteDocumentsConfirmationPage/>)
        expect(screen.getByRole('heading', {name:'Delete health records and attachments'})).toBeInTheDocument();
        expect(screen.getByText(`Are you sure you want to permanently delete all files for patient NHS number ${nhsNumber} ?`)).toBeInTheDocument();
        expect(screen.getByRole('radio', {name:'Yes'})).toBeInTheDocument();
        expect(screen.getByRole('radio', {name:'No'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name:'Continue'})).toBeInTheDocument();
    })
})