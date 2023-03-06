import { render, screen } from "@testing-library/react";
import PatientSummary from "./PatientSummary";
import React from "react";
import { buildPatientDetails } from "../../utils/testBuilders";

describe("<PatientSummary />", () => {
    it("renders a summary with patient details", () => {
        const birthDate = "2003-01-22";
        const familyName = "Smith";
        const givenName = ["Jane"];
        const nhsNumber = "9234567801";
        const postalCode = "LS1 6AE";
        const patientDetails = buildPatientDetails({ birthDate, familyName, givenName, nhsNumber, postalCode });

        render(<PatientSummary patientDetails={patientDetails} />);

        expect(screen.getByText("NHS Number")).toBeInTheDocument();
        expect(screen.getByText(nhsNumber)).toBeInTheDocument();
        expect(screen.getByText("Surname")).toBeInTheDocument();
        expect(screen.getByText(familyName)).toBeInTheDocument();
        expect(screen.getByText("First name")).toBeInTheDocument();
        expect(screen.getByText(givenName[0])).toBeInTheDocument();
        expect(screen.getByText("Date of birth")).toBeInTheDocument();
        expect(screen.getByText("22 January 2003")).toBeInTheDocument();
        expect(screen.getByText("Postcode")).toBeInTheDocument();
        expect(screen.getByText(postalCode)).toBeInTheDocument();
    });
});
