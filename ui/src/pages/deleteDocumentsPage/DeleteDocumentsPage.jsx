import React, { useState } from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import ServiceError from "../../components/serviceError/ServiceError";
import ProgressBar from "../../components/progressBar/ProgressBar";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

const states = {
    IDLE: "idle",
    DELETING: "deleting",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const DeleteDocumentsPage = () => {
    const documentStore = useAuthorisedDocumentStore();
    const { register, handleSubmit } = useForm();
    const navigate = useNavigate();
    const [{ nhsNumber, givenName, familyName }] = usePatientDetailsContext();
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const [session, setSession] = useSessionContext();

    const { ref: shouldDeleteAllDocsRef, ...shouldDeleteAllDocsProps } = register("shouldDeleteAllDocs");

    const doSubmit = async ({ shouldDeleteAllDocs }) => {
        const searchResultsPageUrl = routes.SEARCH_RESULTS;

        if (shouldDeleteAllDocs === "yes") {
            setSubmissionState(states.DELETING);
            try {
                const response = await documentStore.deleteAllDocuments(nhsNumber);

                if (response === "successfully deleted") {
                    setSubmissionState(states.SUCCEEDED);
                    navigate(searchResultsPageUrl);
                }
            } catch (e) {
                if (e.response?.status == 403) {
                    setSession({
                        ...session,
                        isLoggedIn: false,
                    });
                    navigate(routes.ROOT);
                }
                setSubmissionState(states.FAILED);
            }
        } else {
            navigate(searchResultsPageUrl);
        }
    };

    return (
        <>
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
                            value="no"
                            inputRef={shouldDeleteAllDocsRef}
                            defaultChecked
                        >
                            No
                        </Radios.Radio>
                    </Radios>
                </Fieldset>
                {submissionState === states.DELETING && <ProgressBar status="Deleting..."></ProgressBar>}
                <Button type="submit" disabled={submissionState === states.DELETING}>
                    Continue
                </Button>
            </form>
        </>
    );
};

export default DeleteDocumentsPage;
