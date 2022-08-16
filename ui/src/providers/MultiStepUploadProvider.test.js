import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useMultiStepUploadProviderContext, MultiStepUploadProvider } from "./MultiStepUploadProvider";

const TestComponent = ({ newNhsNumber }) => {
    const [nhsNumber, setNhsNumber] = useMultiStepUploadProviderContext();
    return (
        <div>
            <p>NHS Number: {nhsNumber || 'Null'}</p>
            <button onClick={() => setNhsNumber(newNhsNumber)}>Update NHS Number</button>
        </div>
    )
}

describe('The multi step upload provider', () => {
    
    
    it('provides an NHS number value and setter', () => {
        const nhsNumber = 23456;
        render(
            <MultiStepUploadProvider>
                <TestComponent newNhsNumber={nhsNumber} />
            </MultiStepUploadProvider>
        )
        expect(screen.getByText('NHS Number: Null')).toBeInTheDocument()
        userEvent.click(screen.getByText("Update NHS Number"))
        expect(screen.getByText(`NHS Number: ${nhsNumber}`)).toBeInTheDocument()
    })
})