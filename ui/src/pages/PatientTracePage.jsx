import {Button, ErrorSummary, Fieldset, Input, SummaryList, WarningCallout,} from "nhsuk-react-components";
import React, {useState} from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import {useNhsNumberProviderContext} from "../providers/NhsNumberProvider";
import BackButton from "../components/BackButton";
import useApi from "../apiClients/useApi";

const states = {
    IDLE: 'idle',
    SEARCHING: 'searching',
    SUCCEEDED: 'succeeded',
    FAILED: 'failed',
};

export const PatientTracePage = ({ nextPage, title }) => {
    const client = useApi()
    const { register, formState, getValues, handleSubmit } = useForm();
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber", {
        required: "Please enter a 10 digit NHS number",
        pattern: {
            value: /^[0-9]{10}$/,
            message: 'Please enter a 10 digit NHS number',
        },
    });
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const [patientDetails, setPatientDetails] = useState({});
    const [statusCode, setStatusCode] = useState(null);
    const [, setNhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();

    const doSubmit = async (data) => {
        try {
            setSubmissionState(states.SEARCHING);
            setStatusCode(null);
            const response = await client.getPatientDetails(data.nhsNumber);
            setPatientDetails(response.result.patientDetails);
            setSubmissionState(states.SUCCEEDED);
        } catch (e) {
            if (e.response?.status){
                setStatusCode(e.response.status);
            }
            setSubmissionState(states.FAILED);
        }
    };

    const onNextClicked = () => {
        setNhsNumber(getValues('nhsNumber'));
        navigate(nextPage);
    };

    return (
        <>
            <BackButton/>
            <form onSubmit={handleSubmit(doSubmit)} noValidate>
                {(submissionState === states.FAILED && statusCode !== 404) && (
                    <ErrorSummary
                        aria-labelledby="error-summary-title"
                        role="alert"
                        tabIndex={-1}
                    >
                        <ErrorSummary.Title id="error-summary-title">
                            There is a problem
                        </ErrorSummary.Title>
                        <ErrorSummary.Body>
                            {statusCode === 400 ?
                                <p>The NHS number provided is invalid. Please Retry.</p>
                            :
                                <p>Technical error - Please retry.</p>
                            }
                        </ErrorSummary.Body>
                    </ErrorSummary>
                )}
                <Fieldset>
                    <Fieldset.Legend headingLevel={'h1'} isPageHeading>
                        {title}
                    </Fieldset.Legend>
                    <Input
                        id={'nhs-number-input'}
                        label="NHS number"
                        name="nhsNumber"
                        error={formState.errors.nhsNumber?.message}
                        type="text"
                        {...nhsNumberProps}
                        inputRef={nhsNumberRef}
                        readOnly={submissionState === states.SUCCEEDED}
                    />
                </Fieldset>
                {submissionState === states.SEARCHING && (
                    <p>
                        <progress aria-label={'Loading...'}/>
                    </p>
                )}
                {submissionState === states.SUCCEEDED && (
                        <SummaryList>
                            <SummaryList.Row>
                                <SummaryList.Key>Family Name</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails.familyName}
                                </SummaryList.Value>
                            </SummaryList.Row>
                            <SummaryList.Row>
                                <SummaryList.Key>Given Name</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails.givenName?.map(name => `${name} `)}
                                </SummaryList.Value>
                            </SummaryList.Row>
                            <SummaryList.Row>
                                <SummaryList.Key>DoB</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails.birthDate}
                                </SummaryList.Value>
                            </SummaryList.Row>
                            <SummaryList.Row>
                                <SummaryList.Key>Postcode</SummaryList.Key>
                                <SummaryList.Value>
                                    {patientDetails.postalCode}
                                </SummaryList.Value>
                            </SummaryList.Row>
                        </SummaryList>
                )}

                {(submissionState === states.FAILED && statusCode === 404) && (
                        <WarningCallout>
                            <WarningCallout.Label>
                                Patient Not Found
                            </WarningCallout.Label>
                            <p>
                                Please verify NHS number again.
                            </p>
                        </WarningCallout>
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
