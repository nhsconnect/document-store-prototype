import { Details, ErrorSummary, Table } from "nhsuk-react-components";
import { documentUploadStates } from "../enums/documentUploads";
import { formatSize } from "../utils/utils";

const UploadSummary = ({ nhsNumber, documents }) => {
    const successfulUploads = documents.filter((document) => {
        return document.state === documentUploadStates.SUCCEEDED;
    });

    const failedUploads = documents.filter((document) => {
        return document.state === documentUploadStates.FAILED;
    });

    return (
        <section>
            <h2>Upload Summary</h2>
            <p>Summary of uploaded documents for patient number {nhsNumber}</p>
            <p>Please delete files that were uploaded successfully from your system.</p>
            {successfulUploads.length > 0 && (
                <Details>
                    <Details.Summary aria-label="Show successfully uploaded documents">
                        Successfully uploaded documents
                    </Details.Summary>
                    <Details.Text>
                        <Table
                            responsive
                            caption="Successfully uploaded documents"
                            captionProps={{
                                className: "nhsuk-u-visually-hidden",
                            }}
                        >
                            <Table.Head role="rowgroup">
                                <Table.Row>
                                    <Table.Cell>File Name</Table.Cell>
                                    <Table.Cell>File Size</Table.Cell>
                                </Table.Row>
                            </Table.Head>
                            <Table.Body>
                                {successfulUploads.map((document) => {
                                    return (
                                        <Table.Row key={document.id}>
                                            <Table.Cell>{document.file.name}</Table.Cell>
                                            <Table.Cell>{formatSize(document.file.size)}</Table.Cell>
                                        </Table.Row>
                                    );
                                })}
                            </Table.Body>
                        </Table>
                    </Details.Text>
                </Details>
            )}
            {failedUploads.length > 0 && (
                <ErrorSummary aria-labelledby="failed-document-uploads-summary-title" role="alert" tabIndex={-1}>
                    <ErrorSummary.Title id="failed-document-uploads-summary-title">
                        Some of your documents could not be uploaded
                    </ErrorSummary.Title>
                    <ErrorSummary.Body>
                        <p>
                            You can try to upload the documents again if you wish and/or make a note of the failures for
                            future reference
                        </p>
                        <ErrorSummary.List>
                            {failedUploads.map((document) => {
                                return (
                                    <li key={document.id} className="nhsuk-error-message">
                                        {document.file.name}
                                    </li>
                                );
                            })}
                        </ErrorSummary.List>
                    </ErrorSummary.Body>
                </ErrorSummary>
            )}
        </section>
    );
};

export default UploadSummary;
