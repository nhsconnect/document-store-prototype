import { render, screen } from "@testing-library/react";
import PatientSummary from "./PatientSummary";
import React from "react";

describe("Patient Summary test", () => {
    const patientData = {
        birthDate: "2003-01-22",
        familyName: "Smith",
        givenName: ["Jane"],
        nhsNumber: "9234567801",
        postalCode: "LS1 6AE",
    };

    it("renders a summary with patient details", () => {
        render(<PatientSummary patientDetails={patientData} />);
        expect(screen.getByText(`NHS number ${patientData.nhsNumber}`)).toBeInTheDocument();
        expect(screen.getByText(patientData.postalCode)).toBeInTheDocument();
        expect(screen.getByText(patientData.familyName)).toBeInTheDocument();
        expect(screen.getByText("22nd January 2003")).toBeInTheDocument();
        expect(screen.getByText(patientData.givenName[0])).toBeInTheDocument();
    });
});
