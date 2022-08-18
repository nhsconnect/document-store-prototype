import {
    Button,
    ErrorMessage,
    Input,
    SummaryList,
    WarningCallout,
} from "nhsuk-react-components";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useMultiStepUploadProviderContext } from "../providers/MultiStepUploadProvider";

const states = {
    IDLE: "idle",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

export const PatientTracePage = ({ client }) => {
    const { register, handleSubmit } = useForm();
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const [patientDetails, setPatientDetails] = useState({});
    const [nhsNumber, setNhsNumber] = useMultiStepUploadProviderContext();

    const doSubmit = async (data) => {
        try {
            if (submissionState === states.SUCCEEDED) {
                setNhsNumber(data.nhsNumber);
            } else {
                setSubmissionState(states.SEARCHING);
                const patientData = await client.getPatientDetails(
                    data.nhsNumber
                );
                setPatientDetails(patientData);
                setSubmissionState(states.SUCCEEDED);
            }
        } catch (e) {
            setSubmissionState(states.FAILED);
        }
    };

    return (
        <>
            <h2>Upload Patient Records</h2>
            <form onSubmit={handleSubmit(doSubmit)} noValidate>
                <Input
                    id={"nhs-number-input"}
                    label="Enter NHS number"
                    name="nhsNumber"
                    type="text"
                    {...nhsNumberProps}
                    inputRef={nhsNumberRef}
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
                <Button type="submit">Next</Button>
            </form>
        </>
    );
};
