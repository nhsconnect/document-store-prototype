import { Button, ErrorSummary, Fieldset, Input, WarningCallout } from "nhsuk-react-components";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { usePatientDetailsProviderContext } from "../providers/PatientDetailsProvider";
import BackButton from "../components/BackButton";
import useApi from "../apiClients/useApi";
import PatientSummary from "../components/PatientSummary";
import SimpleProgressBar from "../components/SimpleProgressBar";
import ServiceError from "../components/ServiceError";

const states = {
    IDLE: "idle",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

export const PatientTracePage = ({ nextPage }) => {
    const client = useApi();
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
    const [patientDetails, setPatientDetails] = usePatientDetailsProviderContext();
    const navigate = useNavigate();

    const doSubmit = async (data) => {
        try {
            setSubmissionState(states.SEARCHING);
            setStatusCode(null);
            const response = await client.getPatientDetails(data.nhsNumber);
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
                        <Fieldset.Legend headingLevel={"h1"} isPageHeading>
                            Search for patient
                        </Fieldset.Legend>
                        <Input
                            id={"nhs-number-input"}
                            name="nhsNumber"
                            label="Enter NHS number"
                            hint={
                                "Please search patient's record you wish to upload by 10 digit NHS number. For example, 4857773456."
                            }
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
                            <WarningCallout.Label headingLevel={"h2"}>Patient Not Found</WarningCallout.Label>
                            <p>Please verify NHS number again.</p>
                        </WarningCallout>
                    )}
                    <Button type="submit">Search</Button>
                </form>
            ) : (
                <>
                    <h1>Verify patient details</h1>
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
