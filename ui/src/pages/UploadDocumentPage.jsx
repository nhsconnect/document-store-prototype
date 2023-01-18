import { Button, Fieldset, Table } from "nhsuk-react-components";
import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import produce from "immer";
import { usePatientDetailsProviderContext } from "../providers/PatientDetailsProvider";
import BackButton from "../components/BackButton";
import DocumentsInput from "../components/DocumentsInput";
import { formatSize } from "../utils/utils";
import { documentUploadStates as stateNames, documentUploadSteps } from "../enums/documentUploads";
import UploadSummary from "../components/UploadSummary";
import useApi from "../apiClients/useApi";
import PatientSummary from "../components/PatientSummary";

const uploadStateMessages = {
    [stateNames.SELECTED]: "Waiting...",
    [stateNames.UPLOADING]: "Uploading...",
    [stateNames.SUCCEEDED]: "Uploaded",
    [stateNames.FAILED]: "Upload failed",
};

const UploadDocumentPage = ({ nextPagePath }) => {
    const client = useApi();
    const { handleSubmit, control, watch, getValues, formState, setValue } = useForm();
    const documents = watch("documents");
    const [patientDetails] = usePatientDetailsProviderContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (!patientDetails?.nhsNumber) {
            navigate("/upload/patient-trace");
        }
    }, [patientDetails, navigate]);

    const doSubmit = async (data) => {
        const doUpload = async (document) => {
            await client.uploadDocument(document.file, patientDetails?.nhsNumber, (state, progress) => {
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

        await Promise.all(data.documents.map(doUpload));
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

        return documentUploadSteps.UPLOADING;
    };

    const uploadStep = inferUploadStep();

    return (
        <>
            <BackButton />
            {uploadStep === documentUploadSteps.SELECTING_FILES && (
                <form onSubmit={handleSubmit(doSubmit)} noValidate data-testid="upload-document-form">
                    <Fieldset>
                        <Fieldset.Legend headingLevel={"h1"} isPageHeading>
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
            {uploadStep === documentUploadSteps.UPLOADING && (
            <><h1>Your documents are uploading</h1>
                <Table responsive caption="Your documents are uploading"
                captionProps={{
                className: "nhsuk-u-visually-hidden"
                }}>
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
                </Table></>

            )}
            {uploadStep === documentUploadSteps.COMPLETE && (
                <>
                    <UploadSummary documents={documents} patientDetails={patientDetails}></UploadSummary>
                    <p style={{ fontWeight: "600" }}>{"If you want to upload another patient's health record"}</p>
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

export default UploadDocumentPage;
