import {Button, Details, ErrorMessage, ErrorSummary, Fieldset, Input, Table} from "nhsuk-react-components";
import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import BackButton from "../components/BackButton";
import DocumentsInput from "../components/DocumentsInput";
import { formatSize } from "../utils/utils";
import { documentUploadStates as stateNames, documentUploadSteps } from "../enums/documentUploads";
import useDocumentUploadState from "../hooks/useDocumentUploadState";

const uploadStateMessages = {
    [stateNames.WAITING]: "Waiting...",
    [stateNames.STORING_METADATA]: "Storing document metadata...",
    [stateNames.UPLOADING]: "Uploading document...",
    [stateNames.SUCCEEDED]: "Upload successful",
    [stateNames.FAILED]: "Upload failed"
}

const UploadDocumentPage = ({ client }) => {
    const { handleSubmit, control, getValues, formState } = useForm();
    const { documentUploadStates, uploadStep, onUploadStateChange } = useDocumentUploadState();
    const [nhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (!nhsNumber) {
            navigate("/upload/patient-trace");
        }
    }, [nhsNumber, navigate]);

    const doSubmit = async (data) => {
        const doUpload = async (document, index) => {
            await client.uploadDocument(
                document,
                nhsNumber,
                (state, progress) => {
                    onUploadStateChange(index, state, progress)
                }
            )
        }

        await Promise.all(data.documents.map(doUpload))
    };

    return (
        <>
            <BackButton />
            {uploadStep === documentUploadSteps.SELECTING_FILES && <form onSubmit={handleSubmit(doSubmit)} noValidate data-testid="upload-document-form">
                <Fieldset>
                    <Fieldset.Legend headingLevel={"h1"} isPageHeading>
                        Upload a document
                    </Fieldset.Legend>
                    <Input
                        id={"nhs-number-input"}
                        label="NHS number"
                        name="nhsNumber"
                        type="text"
                        value={nhsNumber}
                        readOnly
                    />
                    <DocumentsInput control={control} />
                </Fieldset>
                <Button
                    type="submit"
                    disabled={formState.isSubmitting}
                >
                    Upload
                </Button>

            </form>
            }
            {uploadStep === documentUploadSteps.UPLOADING && (
                <Table responsive caption="Document upload progress.">
                    <Table.Head role="rowgroup">
                        <Table.Row>
                            <Table.Cell>File Name</Table.Cell>
                            <Table.Cell>File Size</Table.Cell>
                            <Table.Cell>File Upload Progress</Table.Cell>
                        </Table.Row>
                    </Table.Head>
                    <Table.Body>
                        {getValues("documents").map((document, index) => (
                            <Table.Row key={document.name}>
                                <Table.Cell>{document.name}</Table.Cell>
                                <Table.Cell>{formatSize(document.size)}</Table.Cell>
                                <Table.Cell>
                                    <progress
                                        aria-label={`Uploading ${document.name}`}
                                        max="100"
                                        value={documentUploadStates[index]?.progress || 0}
                                    ></progress>
                                    <p role="status" aria-label={`${document.name} upload status`}>
                                        {documentUploadStates[index] && uploadStateMessages[documentUploadStates[index].state]}
                                    </p>
                                </Table.Cell>
                            </Table.Row>
                        ))
                        }
                    </Table.Body>
                </Table>
            )}
            {uploadStep === documentUploadSteps.COMPLETE && (
                <>
                    <h2>Upload Summary</h2>
                    <p>Summary of uploaded documents for patient number {nhsNumber}</p>
                    <Details>
                        <Details.Summary>Successfully uploaded documents</Details.Summary>
                        <Details.Text>
                            <Table responsive caption="Documents uploaded Successfully">
                                <Table.Head role="rowgroup">
                                    <Table.Row>
                                        <Table.Cell>File Name</Table.Cell>
                                        <Table.Cell>File Size</Table.Cell>
                                    </Table.Row>
                                </Table.Head>
                                <Table.Body>
                                {getValues("documents").filter((document, index) => {
                                    return documentUploadStates[index].state === stateNames.SUCCEEDED
                                }).map((document, index) => {
                                       return( <Table.Row key={document.name}>
                                            <Table.Cell>{document.name}</Table.Cell>
                                            <Table.Cell>{formatSize(document.size)}</Table.Cell>
                                        </Table.Row>
                                    )})
                                    }
                                </Table.Body>
                            </Table>
                        </Details.Text>
                    </Details>
                    <ErrorSummary aria-labelledby="failed-documents-upload-summary-title" role="alert" tabIndex={-1}>
                        <ErrorSummary.Title id="failed-documents-summary-title">Some of your documents could not be uploaded</ErrorSummary.Title>
                        <ErrorSummary.Body>
                            <p>You can try to upload the documents again if you wish and/or make a note of the failures for future reference</p>
                            <ErrorSummary.List>
                                {getValues("documents").filter((document, index) => {
                                    return documentUploadStates[index].state === stateNames.FAILED
                                }).map((document, index) => {
                                    return(
                                        <li key={document.name} className="nhsuk-error-message">
                                            {document.name}
                                        </li>
                                    )})
                                }
                            </ErrorSummary.List>
                        </ErrorSummary.Body>
                    </ErrorSummary>
                </>
                )}
        </>
    );
};

export default UploadDocumentPage;
