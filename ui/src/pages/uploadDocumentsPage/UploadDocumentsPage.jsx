import { Button, Fieldset, Table, WarningCallout } from "nhsuk-react-components";
import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import produce from "immer";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import DocumentsInput from "../../components/documentsInput/DocumentsInput";
import { formatSize } from "../../utils/utils";
import { documentUploadStates as stateNames, documentUploadSteps } from "../../enums/documentUploads";
import UploadSummary from "../../components/uploadSummary/UploadSummary";
import PatientSummary from "../../components/patientSummary/PatientSummary";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import routes from "../../enums/routes";

const UploadDocumentsPage = ({ nextPagePath }) => {
    const documentStore = useAuthorisedDocumentStore();
    const { handleSubmit, control, watch, getValues, formState, setValue } = useForm();
    const [patientDetails] = usePatientDetailsContext();
    const navigate = useNavigate();

    const documents = watch("documents");
    const uploadStateMessages = {
        [stateNames.SELECTED]: "Waiting...",
        [stateNames.UPLOADING]: "Uploading...",
        [stateNames.SUCCEEDED]: "Uploaded",
        [stateNames.FAILED]: "Upload failed",
    };

    useEffect(() => {
        if (!patientDetails?.nhsNumber) {
            navigate(routes.UPLOAD_SEARCH_PATIENT);
        }
    }, [patientDetails, navigate]);

    const uploadDocuments = async (data) => {
        await Promise.all(data.documents.map(uploadDocument));
    };

    const uploadDocument = async (document) => {
        await documentStore.uploadDocument(document.file, patientDetails?.nhsNumber, (state, progress) => {
            setValue(
                "documents",
                produce(getValues("documents"), (draft) => {
                    const documentIndex = draft.findIndex((draftDocument) => draftDocument.id === document.id);
                    draft[documentIndex].state = state;
                    draft[documentIndex].progress = progress;
                })
            );
        });
    };

    const inferUploadStep = () => {
        if (!documents || documents.every((document) => document.state === stateNames.SELECTED)) {
            return documentUploadSteps.SELECTING_FILES;
        }

        if (
            documents.every((document) => {
                return document.state === stateNames.SUCCEEDED || document.state === stateNames.FAILED;
            })
        ) {
            return documentUploadSteps.COMPLETE;
        }
        if (documents.every((document) => document.state === stateNames.UNAUTHORISED)) {
            navigate(routes.ROOT);
        }
        return documentUploadSteps.UPLOADING;
    };

    return (
        <>
            {inferUploadStep() === documentUploadSteps.SELECTING_FILES && (
                <form onSubmit={handleSubmit(uploadDocuments)} noValidate data-testid="upload-document-form">
                    <Fieldset>
                        <Fieldset.Legend headingLevel="h1" isPageHeading>
                            Upload documents
                        </Fieldset.Legend>
                        <PatientSummary patientDetails={patientDetails} />
                        <DocumentsInput control={control} />
                    </Fieldset>
                    <Button type="submit" disabled={formState.isSubmitting}>
                        Upload
                    </Button>
                </form>
            )}
            {inferUploadStep() === documentUploadSteps.UPLOADING && (
                <>
                    <h1>Your documents are uploading</h1>
                    <WarningCallout>
                        <WarningCallout.Label>Stay on this page</WarningCallout.Label>
                        <p>Do not close or navigate away from this browser until upload is complete.</p>
                    </WarningCallout>
                    <Table
                        responsive
                        caption="Your documents are uploading"
                        captionProps={{
                            className: "nhsuk-u-visually-hidden",
                        }}
                    >
                        <Table.Head role="rowgroup">
                            <Table.Row>
                                <Table.Cell>File Name</Table.Cell>
                                <Table.Cell>File Size</Table.Cell>
                                <Table.Cell>File Upload Progress</Table.Cell>
                            </Table.Row>
                        </Table.Head>
                        <Table.Body>
                            {documents.map((document) => (
                                <Table.Row key={document.id}>
                                    <Table.Cell>{document.file.name}</Table.Cell>
                                    <Table.Cell>{formatSize(document.file.size)}</Table.Cell>
                                    <Table.Cell>
                                        <progress
                                            aria-label={`Uploading ${document.file.name}`}
                                            max="100"
                                            value={document.progress}
                                        ></progress>
                                        <p role="status" aria-label={`${document.file.name} upload status`}>
                                            {uploadStateMessages[document.state]}
                                        </p>
                                    </Table.Cell>
                                </Table.Row>
                            ))}
                        </Table.Body>
                    </Table>
                </>
            )}
            {inferUploadStep() === documentUploadSteps.COMPLETE && (
                <>
                    <UploadSummary documents={documents} patientDetails={patientDetails}></UploadSummary>
                    <p style={{ fontWeight: "600" }}>If you want to upload another patient&apos;s health record</p>
                    <Button
                        onClick={() => {
                            navigate(nextPagePath);
                        }}
                    >
                        Start Again
                    </Button>
                </>
            )}
        </>
    );
};

export default UploadDocumentsPage;
