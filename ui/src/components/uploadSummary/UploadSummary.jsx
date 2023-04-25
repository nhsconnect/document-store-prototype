import { Details, Table, WarningCallout } from "nhsuk-react-components";
import { documentUploadStates } from "../../enums/documentUploads";
import { formatSize, getFormattedDate } from "../../utils/utils";
import PatientSummary from "../patientSummary/PatientSummary";
import React from "react";
import ErrorBox from "../errorBox/ErrorBox";

const UploadSummary = ({ patientDetails, documents }) => {
    const successfulUploads = documents.filter((document) => {
        return document.state === documentUploadStates.SUCCEEDED;
    });
    const failedUploads = documents.filter((document) => {
        return document.state === documentUploadStates.FAILED;
    });
    const tableMargin = { marginBottom: 50 };
    const tableCaption = (
        <>
            <h3>Failed uploads</h3>
            <span className="nhsuk-error-message" id="example-error">
                <span className="nhsuk-u-visually-hidden">Error:</span> Documents that have failed to upload
            </span>
        </>
    );

    return (
        <section>
            {failedUploads.length > 0 && (
                <ErrorBox
                    errorBoxSummaryId={"failed-document-uploads-summary-title"}
                    errorInputLink={"#failed-uploads"}
                    messageTitle={"Some of your documents failed to upload"}
                    messageLinkBody={"Documents that have failed to upload"}
                    messageBody={
                        "You can try to upload the documents again if you wish and/or make a note of the failures for future reference."
                    }
                ></ErrorBox>
            )}
            <h1>Upload Summary</h1>
            {failedUploads.length > 0 && (
                <div className={"nhsuk-form-group--error"}>
                    <Table responsive caption={tableCaption} style={tableMargin} id="failed-uploads">
                        <Table.Body>
                            {failedUploads.map((document) => {
                                return (
                                    <Table.Row key={document.id}>
                                        <Table.Cell>{document.file.name}</Table.Cell>
                                        <Table.Cell>{formatSize(document.file.size)}</Table.Cell>
                                    </Table.Row>
                                );
                            })}
                        </Table.Body>
                    </Table>
                </div>
            )}
            {failedUploads.length === 0 && (
                <h2>All documents have been successfully uploaded on {getFormattedDate(new Date())}</h2>
            )}
            {successfulUploads.length > 0 && (
                <>
                    <Details style={tableMargin}>
                        <Details.Summary aria-label="View successfully uploaded documents">
                            View successfully uploaded documents
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
                </>
            )}
            <PatientSummary patientDetails={patientDetails} />
            <WarningCallout style={{ marginTop: 75 }}>
                <WarningCallout.Label>Before you close this page</WarningCallout.Label>
                <ul>
                    <li>
                        We recommend that you take a screenshot of this summary page and attach it to the patient&apos;s
                        record
                    </li>
                    <li>
                        When you have finished uploading documents for this patient and they are deducted from your
                        practice, please delete any temporary files created for upload on your computer
                    </li>
                </ul>
            </WarningCallout>
        </section>
    );
};

export default UploadSummary;
