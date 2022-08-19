import {
    Button,
    ErrorMessage,
    Input,
    SummaryList,
    WarningCallout,
} from "nhsuk-react-components";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { useMultiStepUploadProviderContext } from "../providers/MultiStepUploadProvider";

const states = {
    IDLE: "idle",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

export const PatientTracePage = ({ client }) => {
    const { register, formState, getValues, handleSubmit } = useForm();
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber", {
        required: "Please enter a 10 digit NHS number",
        pattern: {
            value: /^[0-9]{10}$/,
            message: "Please enter a 10 digit NHS number",
        },
    });
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const [patientDetails, setPatientDetails] = useState({});
    const [nhsNumber, setNhsNumber] = useMultiStepUploadProviderContext();
    const navigate = useNavigate();

    const doSubmit = async (data) => {
        try {
            setSubmissionState(states.SEARCHING);
            const patientData = await client.getPatientDetails(data.nhsNumber);
            setPatientDetails(patientData);
            setSubmissionState(states.SUCCEEDED);
        } catch (e) {
            setSubmissionState(states.FAILED);
        }
    };

    const onNextClicked = () => {
        setNhsNumber(getValues("nhsNumber"));
        navigate("/upload/submit");
    };

    return (
        <>
            <h2>Upload Patient Records</h2>
            <form onSubmit={handleSubmit(doSubmit)} noValidate>
                <Input
                    id={"nhs-number-input"}
                    label="Enter NHS number"
                    name="nhsNumber"
                    error={formState.errors.nhsNumber?.message}
                    type="text"
                    {...nhsNumberProps}
                    inputRef={nhsNumberRef}
                    disabled={submissionState === states.SUCCEEDED}
                />
                {submissionState === states.SEARCHING && (
                    <p>
                        <progress aria-label={"Loading..."} />
                    </p>
                )}
                {submissionState === states.SUCCEEDED &&
                    patientDetails.length > 0 && (
                        <SummaryList>
                            <SummaryList.Row>
                                <SummaryList.Key>Name</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails[0].name}
                                </SummaryList.Value>
                            </SummaryList.Row>
                            <SummaryList.Row>
                                <SummaryList.Key>DoB</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails[0].dateOfBirth}
                                </SummaryList.Value>
                            </SummaryList.Row>
                            <SummaryList.Row>
                                <SummaryList.Key>Postcode</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails[0].postcode}
                                </SummaryList.Value>
                            </SummaryList.Row>
                        </SummaryList>
                    )}
                {submissionState === states.SUCCEEDED &&
                    patientDetails.length === 0 && (
                        <WarningCallout>
                            <WarningCallout.Label>
                                Patient Not Found
                            </WarningCallout.Label>
                            <p>
                                Please verify NHS number again. However, if you
                                are sure it's correct you can proceed.
                            </p>
                        </WarningCallout>
                    )}
                {submissionState === states.FAILED && (
                    <ErrorMessage>
                        Technical Failure - Please retry.
                    </ErrorMessage>
                )}
                {(submissionState === states.IDLE ||
                    submissionState.FAILED ||
                    submissionState.SEARCHING) && (
                    <Button type="submit">Search</Button>
                )}
                {submissionState === states.SUCCEEDED && (
                    <Button onClick={onNextClicked}>Next</Button>
                )}
            </form>
        </>
    );
};
