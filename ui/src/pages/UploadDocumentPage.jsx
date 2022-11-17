import { Button, Fieldset, Input, Table } from "nhsuk-react-components";
import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import BackButton from "../components/BackButton";
import DocumentsInput from "../components/DocumentsInput";
import { produce } from "immer"
import { formatSize } from "../utils/utils";
import { documentUploadStates as stateNames } from "../apiClients/apiClient";

const submissionStates = {
    IDLE: "idle",
    UPLOADING: "uploading",
    COMPLETE: "complete",
};

const uploadStateMessages = {
    [stateNames.WAITING]: "Waiting...",
    [stateNames.STORING_METADATA]: "Storing document metadata...",
    [stateNames.UPLOADING]: "Uploading document...",
    [stateNames.SUCCEEDED]: "Upload successful",
    [stateNames.FAILED]: "Upload failed"
}

const UploadDocumentPage = ({ client }) => {
    const { handleSubmit, control, getValues } = useForm();
    const [submissionState, setSubmissionState] = useState(submissionStates.IDLE);
    const [documentUploadStates, setDocumentUploadStates] = useState([]);
    const [nhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (!nhsNumber) {
            navigate("/upload/patient-trace");
        }
    }, [nhsNumber, navigate]);

    const doSubmit = async (data) => {
        setSubmissionState(submissionStates.UPLOADING);

        const doUpload = async (document, index) => {
            await client.uploadDocument(
                document,
                nhsNumber,
                (state, progress) => {
                    setDocumentUploadStates(current => {
                        return produce(current, draft => {
                            draft[index] = {
                                state,
                                progress
                            }
                        })
                    })
                }
            )
        }

        await Promise.all(data.documents.map((document, index) => doUpload(document, index)))
        setSubmissionState(submissionStates.COMPLETE)
    };

    return (
        <>
            <BackButton />
            {submissionState === submissionStates.IDLE && <form onSubmit={handleSubmit(doSubmit)} noValidate data-testid="upload-document-form">
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
                    disabled={submissionState === submissionStates.UPLOADING}
                >
                    Upload
                </Button>

            </form>
            }
            {submissionState === submissionStates.UPLOADING && (
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
            {submissionState === submissionStates.COMPLETE && (
                <h2>Upload Summary</h2>
            )}
        </>
    );
};

export default UploadDocumentPage;
