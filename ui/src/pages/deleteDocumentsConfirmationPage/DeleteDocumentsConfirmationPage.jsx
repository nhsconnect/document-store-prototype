import React, { useState } from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import BackButton from "../../components/backButton/BackButton";
import { usePatientDetailsProviderContext } from "../../providers/PatientDetailsProvider";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import ServiceError from "../../components/serviceError/ServiceError";
import SimpleProgressBar from "../../components/simpleProgressBar/SimpleProgressBar";
import { useDocumentStore } from "../../apiClients/documentStore";

const states = {
    IDLE: "idle",
    DELETING: "deleting",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const DeleteDocumentsConfirmationPage = () => {
    const documentStore = useDocumentStore();
    const { register, handleSubmit } = useForm();
    const navigate = useNavigate();
    const [{ nhsNumber, givenName, familyName }] = usePatientDetailsProviderContext();
    const [submissionState, setSubmissionState] = useState(states.IDLE);

    const { ref: shouldDeleteAllDocsRef, ...shouldDeleteAllDocsProps } = register("shouldDeleteAllDocs");

    const doSubmit = async ({ shouldDeleteAllDocs }) => {
        const searchResultsPageUrl = "/search/results";

        if (shouldDeleteAllDocs === "yes") {
            setSubmissionState(states.DELETING);
            try {
                const response = await documentStore.deleteAllDocuments(nhsNumber);

                if (response === "successfully deleted") {
                    setSubmissionState(states.SUCCEEDED);
                    navigate(searchResultsPageUrl);
                }
            } catch (error) {
                setSubmissionState(states.FAILED);
            }
        } else {
            navigate(searchResultsPageUrl);
        }
    };

    return (
        <>
            <BackButton />
            {submissionState === states.FAILED && (
                <ServiceError message="There has been an issue deleting these records, please try again later." />
            )}
            <form onSubmit={handleSubmit(doSubmit)}>
                <Fieldset>
                    <Fieldset.Legend isPageHeading>Delete health records and attachments</Fieldset.Legend>
                    <Fieldset.Legend size="m">
                        Are you sure you want to permanently delete all files for patient {givenName?.join(" ")}{" "}
                        {familyName} NHS number {nhsNumber}?
                    </Fieldset.Legend>
                    <Radios>
                        <Radios.Radio
                            {...shouldDeleteAllDocsProps}
                            id="yes"
                            value="yes"
                            inputRef={shouldDeleteAllDocsRef}
                        >
                            Yes
                        </Radios.Radio>
                        <Radios.Radio
                            {...shouldDeleteAllDocsProps}
                            id="no"
                            value="No"
                            inputRef={shouldDeleteAllDocsRef}
                            defaultChecked
                        >
                            No
                        </Radios.Radio>
                    </Radios>
                </Fieldset>
                {submissionState === states.DELETING && <SimpleProgressBar status="Deleting..."></SimpleProgressBar>}
                <Button type="submit" disabled={submissionState === states.DELETING}>
                    Continue
                </Button>
            </form>
        </>
    );
};

export default DeleteDocumentsConfirmationPage;
