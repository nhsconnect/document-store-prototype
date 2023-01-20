import React, { useState } from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import BackButton from "../components/BackButton";
import { usePatientDetailsProviderContext } from "../providers/PatientDetailsProvider";
import useApi from "../apiClients/useApi";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import ServiceError from "../components/ServiceError";
import SimpleProgressBar from "../components/SimpleProgressBar";

const states = {
    IDLE: "idle",
    DELETING: "deleting",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const DeleteDocumentsConfirmationPage = () => {
    const client = useApi();
    const { register, handleSubmit } = useForm();
    let navigate = useNavigate();
    const [patientDetails] = usePatientDetailsProviderContext();
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const { ref: trxRef, ...trxProps } = register("trx");

    const doSubmit = async (data) => {
        if (data.trx === "yes") {
            setSubmissionState(states.DELETING);
            try {
                const response = await client.deleteAllDocuments(patientDetails.nhsNumber);
                if (response === "successfully deleted") {
                    setSubmissionState(states.SUCCEEDED);
                    navigate("/search/results");
                }
            } catch (error) {
                setSubmissionState(states.FAILED);
            }
        } else {
            navigate("/search/results");
        }
    };

    return (
        <>
            <BackButton />
            {submissionState === states.FAILED && (
                <ServiceError message={"There has been an issue deleting these records, please try again later."} />
            )}
            <form onSubmit={handleSubmit(doSubmit)}>
                <Fieldset>
                    <Fieldset.Legend isPageHeading>Delete health records and attachments</Fieldset.Legend>
                    <Fieldset.Legend size="m">
                        Are you sure you want to permanently delete all files for patient{" "}
                        {patientDetails.givenName?.join(" ")} {patientDetails.familyName} NHS number{" "}
                        {patientDetails.nhsNumber} ?
                    </Fieldset.Legend>
                    <Radios name="delete-documents-action">
                        <Radios.Radio id="yes" value="yes" inputRef={trxRef} {...trxProps}>
                            Yes
                        </Radios.Radio>
                        <Radios.Radio id="no" value="no" inputRef={trxRef} {...trxProps} defaultChecked>
                            No
                        </Radios.Radio>
                    </Radios>
                </Fieldset>
                {submissionState === states.DELETING && <SimpleProgressBar status="Deleting..."></SimpleProgressBar>}
                <Button type={"submit"} disabled={submissionState === states.DELETING}>
                    Continue
                </Button>
            </form>
        </>
    );
};

export default DeleteDocumentsConfirmationPage;
