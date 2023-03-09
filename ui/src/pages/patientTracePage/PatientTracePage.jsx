import { Button, ErrorSummary, Fieldset, Input, WarningCallout } from "nhsuk-react-components";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { usePatientDetailsContext } from "../../providers/PatientDetailsProvider";
import BackButton from "../../components/backButton/BackButton";
import PatientSummary from "../../components/patientSummary/PatientSummary";
import SimpleProgressBar from "../../components/simpleProgressBar/SimpleProgressBar";
import ServiceError from "../../components/serviceError/ServiceError";
import { useAuthorisedDocumentStore } from "../../providers/DocumentStoreProvider";

const states = {
    IDLE: "idle",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

export const PatientTracePage = ({ nextPage }) => {
    const documentStore = useAuthorisedDocumentStore();
    const { register, formState, handleSubmit } = useForm();
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber", {
        required: "Please enter a 10 digit NHS number",
        pattern: {
            value: /^[0-9]{10}$/,
            message: "Please enter a 10 digit NHS number",
        },
    });
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const [statusCode, setStatusCode] = useState(null);
    const [patientDetails, setPatientDetails] = usePatientDetailsContext();
    const navigate = useNavigate();

    const doSubmit = async (data) => {
        try {
            setSubmissionState(states.SEARCHING);
            setStatusCode(null);
            const response = await documentStore.getPatientDetails(data.nhsNumber);
            setPatientDetails(response.result.patientDetails);
            setSubmissionState(states.SUCCEEDED);
        } catch (e) {
            if (e.response?.status) {
                setStatusCode(e.response.status);
            }
            setSubmissionState(states.FAILED);
        }
    };

    const onNextClicked = () => {
        navigate(nextPage);
    };

    return (
        <>
            <BackButton />
            {submissionState !== states.SUCCEEDED ? (
                <form onSubmit={handleSubmit(doSubmit)} noValidate>
                    {submissionState === states.FAILED && statusCode !== 404 && (
                        <>
                            {statusCode === 400 ? (
                                <ErrorSummary aria-labelledby="error-summary-title" role="alert" tabIndex={-1}>
                                    <ErrorSummary.Title id="error-summary-title">There is a problem</ErrorSummary.Title>
                                    <ErrorSummary.Body>
                                        <p>
                                            The NHS number provided is invalid. Please check the number you have
                                            entered.
                                        </p>
                                    </ErrorSummary.Body>
                                </ErrorSummary>
                            ) : (
                                <ServiceError></ServiceError>
                            )}
                        </>
                    )}
                    <Fieldset>
                        <Fieldset.Legend headingLevel="h1" isPageHeading>
                            Search for patient
                        </Fieldset.Legend>
                        <Input
                            id="nhs-number-input"
                            name="nhsNumber"
                            label="Enter NHS number"
                            hint="A 10-digit number, for example, 485 777 3456"
                            error={formState.errors.nhsNumber?.message}
                            type="text"
                            {...nhsNumberProps}
                            inputRef={nhsNumberRef}
                            readOnly={submissionState === states.SUCCEEDED}
                        />
                    </Fieldset>
                    {submissionState === states.SEARCHING && (
                        <SimpleProgressBar status="Searching..."></SimpleProgressBar>
                    )}
                    {submissionState === states.FAILED && statusCode === 404 && (
                        <WarningCallout>
                            <WarningCallout.Label headingLevel="h2">Patient Not Found</WarningCallout.Label>
                            <p>Please verify NHS number again.</p>
                        </WarningCallout>
                    )}
                    <Button type="submit">Search</Button>
                </form>
            ) : (
                <>
                    <h1>Verify patient details</h1>
                    {(patientDetails.superseded || patientDetails.restricted) && (
                        <WarningCallout>
                            <WarningCallout.Label headingLevel="h2">Information</WarningCallout.Label>
                            {patientDetails.superseded && <p>The NHS number for this patient has changed.</p>}
                            {patientDetails.restricted && (
                                <p>
                                    Certain details about this patient cannot be displayed without the necessary access.
                                </p>
                            )}
                        </WarningCallout>
                    )}
                    <PatientSummary patientDetails={patientDetails} />
                    {nextPage?.includes("upload") && (
                        <p>
                            Ensure these patient details match the electronic health records and attachments you are
                            about to upload.
                        </p>
                    )}
                    <Button onClick={onNextClicked}>Next</Button>
                </>
            )}
        </>
    );
};
