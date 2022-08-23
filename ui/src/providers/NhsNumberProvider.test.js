import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
    useNhsNumberProviderContext,
    NhsNumberProvider,
} from "./NhsNumberProvider";

const TestComponent = ({ newNhsNumber }) => {
    const [nhsNumber, setNhsNumber] = useNhsNumberProviderContext();
    return (
        <div>
            <p>NHS Number: {nhsNumber || "Null"}</p>
            <button onClick={() => setNhsNumber(newNhsNumber)}>
                Update NHS Number
            </button>
        </div>
    );
};

describe("The NHS number provider", () => {
    it("provides an NHS number value and setter", () => {
        const nhsNumber = 23456;
        render(
            <NhsNumberProvider>
                <TestComponent newNhsNumber={nhsNumber} />
            </NhsNumberProvider>
        );
        expect(screen.getByText("NHS Number: Null")).toBeInTheDocument();

        userEvent.click(screen.getByText("Update NHS Number"));

        expect(
            screen.getByText(`NHS Number: ${nhsNumber}`)
        ).toBeInTheDocument();
    });
});
