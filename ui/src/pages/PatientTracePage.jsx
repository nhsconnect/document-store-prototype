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
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";

const states = {
    IDLE: "idle",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

export const PatientTracePage = ({ client, nextPage, title }) => {
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
    const [nhsNumber, setNhsNumber] = useNhsNumberProviderContext();
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
        navigate(nextPage);
    };

    return (
        <>
            <h2>{title}</h2>
            <form onSubmit={handleSubmit(doSubmit)} noValidate>
                <Input
                    id={"nhs-number-input"}
                    label="Enter NHS number"
                    name="nhsNumber"
                    error={formState.errors.nhsNumber?.message}
                    type="text"
                    {...nhsNumberProps}
                    inputRef={nhsNumberRef}
                    readOnly={submissionState === states.SUCCEEDED}
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
                                <SummaryList.Key>Family Name</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails[0].name.family}
                                </SummaryList.Value>
                            </SummaryList.Row>
                            <SummaryList.Row>
                                <SummaryList.Key>Given Name</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails[0].name.given[0]}
                                </SummaryList.Value>
                            </SummaryList.Row>
                            <SummaryList.Row>
                                <SummaryList.Key>DoB</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails[0].dateOfBirth.toLocaleDateString()}
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
                    submissionState === states.FAILED ||
                    submissionState === states.SEARCHING) && (
                    <Button type="submit">Search</Button>
                )}
            </form>
            {submissionState === states.SUCCEEDED && (
                <Button onClick={onNextClicked}>Next</Button>
            )}
        </>
    );
};
