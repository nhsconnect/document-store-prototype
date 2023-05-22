import { Button, Fieldset, Input } from "nhsuk-react-components";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import BackButton from "../../components/backButton/BackButton";
import ProgressBar from "../../components/progressBar/ProgressBar";
import ServiceError from "../../components/serviceError/ServiceError";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import ErrorBox from "../../components/errorBox/ErrorBox";
import { useNavigate } from "react-router";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

const states = {
    IDLE: "idle",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

export const PatientTracePage = ({ nextPage }) => {
    const documentStore = useAuthorisedDocumentStore();
    const { register, handleSubmit } = useForm({ reValidateMode: "onSubmit" });
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber", {
        required: "Enter patient's 10 digit NHS number",
        pattern: {
            value: /(^[0-9]{10}$|^[0-9]{3}\s[0-9]{3}\s[0-9]{4}$|^[0-9]{3}-[0-9]{3}-[0-9]{4}$)/,
            message: "Enter patient's 10 digit NHS number",
        },
    });
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const setPatientDetails = usePatientDetailsContext()[1];
    const [session, setSession] = useSessionContext();
    const [inputError, setInputError] = useState(null);
    const [statusCode, setStatusCode] = useState(null);

    const navigate = useNavigate();

    const doSubmit = async (data) => {
        try {
            setInputError(null);
            setStatusCode(null);
            setSubmissionState(states.SEARCHING);

            const nhsNumber = data.nhsNumber.replace(/[-\s]/gi, "");
            const response = await documentStore.getPatientDetails(nhsNumber);
            setPatientDetails(response.result.patientDetails);
            setSubmissionState(states.SUCCEEDED);
            navigate(nextPage);
        } catch (e) {
            setStatusCode(e.response?.status ?? null);
            if (e.response?.status === 403) {
                setSession({
                    ...session,
                    isLoggedIn: "false",
                });
                navigate(routes.ROOT);
            } else if (e.response?.status === 404) {
                setInputError("Enter a valid patient NHS number");
            }
            setSubmissionState(states.FAILED);
        }
    };

    const onError = async () => {
        setSubmissionState(states.FAILED);
        setInputError("Enter patient's 10 digit NHS number");
    };

    return (
        <>
            <BackButton />
            <>
                {submissionState === states.FAILED && (
                    <>
                        {statusCode >= 500 || !inputError ? (
                            <ServiceError />
                        ) : (
                            <ErrorBox
                                messageTitle={"There is a problem"}
                                messageLinkBody={inputError}
                                errorInputLink={"#nhs-number-input"}
                                errorBoxSummaryId={"error-box-summary"}
                            />
                        )}
                    </>
                )}
                <form onSubmit={handleSubmit(doSubmit, onError)} noValidate>
                    <Fieldset>
                        <Fieldset.Legend headingLevel="h1" isPageHeading>
                            Search for patient
                        </Fieldset.Legend>
                        <Input
                            id="nhs-number-input"
                            name="nhsNumber"
                            label="Enter NHS number"
                            hint="A 10-digit number, for example, 485 777 3456"
                            error={submissionState !== states.SEARCHING && inputError}
                            type="text"
                            {...nhsNumberProps}
                            inputRef={nhsNumberRef}
                            readOnly={submissionState === states.SUCCEEDED || submissionState === states.SEARCHING}
                        />
                    </Fieldset>
                    {submissionState === states.SEARCHING && <ProgressBar status="Searching..."></ProgressBar>}
                    <Button type="submit">Search</Button>
                </form>
            </>
        </>
    );
};
