import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { usePatientDetailsProviderContext, PatientDetailsProvider } from "./PatientDetailsProvider";

const TestComponent = ({ newPatientDetails }) => {
    const [patientDetails, setPatientDetails] = usePatientDetailsProviderContext();
    return (
        <div>
            <p>NHS Number: {patientDetails?.nhsNumber || "Null"}</p>
            <p>Family Name: {patientDetails?.familyName || "Null"}</p>
            <button onClick={() => setPatientDetails(newPatientDetails)}>Update NHS Number</button>
        </div>
    );
};

describe("The NHS number provider", () => {
    it("provides an NHS number value and setter", () => {
        const patientDetails = {
            nhsNumber: 23456,
            familyName: "Smith",
        };
        render(
            <PatientDetailsProvider>
                <TestComponent newPatientDetails={patientDetails} />
            </PatientDetailsProvider>
        );
        expect(screen.getByText("NHS Number: Null")).toBeInTheDocument();
        expect(screen.getByText("Family Name: Null")).toBeInTheDocument();

        userEvent.click(screen.getByText("Update NHS Number"));

        expect(screen.getByText(`NHS Number: ${patientDetails.nhsNumber}`)).toBeInTheDocument();
        expect(screen.getByText(`Family Name: ${patientDetails.familyName}`)).toBeInTheDocument();
    });
});
