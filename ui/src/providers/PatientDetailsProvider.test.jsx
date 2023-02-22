import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PatientDetailsProvider, usePatientDetailsProviderContext } from "./PatientDetailsProvider";

describe("<PatientDetailsProvider />", () => {
    it("provides NHS number and family name", () => {
        const patientDetails = {
            nhsNumber: 23456,
            familyName: "Smith",
        };

        render(
            <PatientDetailsProvider>
                <TestComponent patientDetails={patientDetails} />
            </PatientDetailsProvider>
        );

        expect(screen.getByText("NHS Number: Null")).toBeInTheDocument();
        expect(screen.getByText("Family Name: Null")).toBeInTheDocument();

        userEvent.click(screen.getByRole("button", { name: "Update NHS Number" }));

        expect(screen.getByText(`NHS Number: ${patientDetails.nhsNumber}`)).toBeInTheDocument();
        expect(screen.getByText(`Family Name: ${patientDetails.familyName}`)).toBeInTheDocument();
    });
});

const TestComponent = (props) => {
    const [patientDetails, setPatientDetails] = usePatientDetailsProviderContext();

    return (
        <>
            <p>NHS Number: {patientDetails?.nhsNumber || "Null"}</p>
            <p>Family Name: {patientDetails?.familyName || "Null"}</p>
            <button onClick={() => setPatientDetails(props.patientDetails)}>Update NHS Number</button>
        </>
    );
};
