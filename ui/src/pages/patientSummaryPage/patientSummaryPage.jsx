import { Button, WarningCallout } from "nhsuk-react-components";
import React from "react";
import { useNavigate } from "react-router";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import BackButton from "../../components/backButton/BackButton";
import PatientSummary from "../../components/patientSummary/PatientSummary";
import routes from "../../enums/routes";

export const PatientSummaryPage = ({ nextPage }) => {
    const [patientDetails] = usePatientDetailsContext();
    const navigate = useNavigate();

    const onNextClicked = () => {
        navigate(nextPage);
    };

    if (!patientDetails) {
        navigate(routes.HOME);
    }

    return (
        <>
            <BackButton />
            <h1>Verify patient details</h1>
            {(patientDetails.superseded || patientDetails.restricted) && (
                <WarningCallout>
                    <WarningCallout.Label headingLevel="h2">Information</WarningCallout.Label>
                    {patientDetails.superseded && <p>The NHS number for this patient has changed.</p>}
                    {patientDetails.restricted && (
                        <p>Certain details about this patient cannot be displayed without the necessary access.</p>
                    )}
                </WarningCallout>
            )}
            <PatientSummary patientDetails={patientDetails} />
            {nextPage?.includes("upload") && (
                <p>
                    Ensure these patient details match the electronic health records and attachments you are about to
                    upload.
                </p>
            )}
            <Button onClick={onNextClicked}>Next</Button>
        </>
    );
};
