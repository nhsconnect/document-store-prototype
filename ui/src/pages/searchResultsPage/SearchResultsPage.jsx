import React, { useEffect, useState } from "react";
import { Button, Table } from "nhsuk-react-components";
import { usePatientDetailsContext } from "../../providers/PatientDetailsProvider";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";
import BackButton from "../../components/backButton/BackButton";
import { downloadFile } from "../../utils/utils";
import PatientSummary from "../../components/patientSummary/PatientSummary";
import ProgressBar from "../../components/progressBar/ProgressBar";
import ServiceError from "../../components/serviceError/ServiceError";
import { useAuthorisedDocumentStore } from "../../providers/DocumentStoreProvider";
import routes from "../../enums/routes";

const states = {
    INITIAL: "initial",
    PENDING: "pending",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const SearchResultsPage = () => {
    const documentStore = useAuthorisedDocumentStore();
    const [searchResults, setSearchResults] = useState([]);
    const [submissionState, setSubmissionState] = useState(states.INITIAL);
    const [downloadState, setDownloadState] = useState(states.INITIAL);
    const [patientDetails] = usePatientDetailsContext();
    const navigate = useNavigate();
    const [hasCleanFiles, setHasCleanFiles] = useState(false)

    useEffect(() => {
        if (!patientDetails?.nhsNumber) {
            navigate(routes.SEARCH_PATIENT);
            return;
        }
        const search = async () => {
            setSubmissionState(states.PENDING);
            setSearchResults([]);
            try {
                const results = await documentStore.findByNhsNumber(patientDetails.nhsNumber);
                results.sort((a, b) => (a.indexed < b.indexed ? 1 : -1));
                setSearchResults(results);
                setHasCleanFiles(results.some((doc) => (doc.virusScanResult === "Clean")))
                setSubmissionState(states.SUCCEEDED);
            } catch (error) {
                setSubmissionState(states.FAILED);
            }
        };

        void search();
    }, [documentStore, patientDetails, navigate, setSubmissionState, setSearchResults]);

    const downloadAll = async () => {
        setDownloadState(states.PENDING);
        try {
            const preSignedUrl = await documentStore.getPresignedUrlForZip(patientDetails.nhsNumber);

            downloadFile(preSignedUrl, `patient-record-${patientDetails.nhsNumber}`);

            setDownloadState(states.SUCCEEDED);
        } catch (e) {
            setDownloadState(states.FAILED);
        }
    };

    return (
        <>
            <BackButton />
            <h1>Download electronic health records and attachments</h1>
            {(submissionState === states.FAILED || downloadState === states.FAILED) && <ServiceError />}
            <PatientSummary patientDetails={patientDetails} />
            {submissionState === states.PENDING && <ProgressBar status="Loading..."></ProgressBar>}
            {submissionState === states.SUCCEEDED && (
                <>
                    {searchResults.length > 0 && (
                        <>
                            <Table caption="List of documents available">
                                <Table.Head>
                                    <Table.Row>
                                        <Table.Cell>Filename</Table.Cell>
                                        <Table.Cell>Uploaded At</Table.Cell>
                                    </Table.Row>
                                </Table.Head>
                                <Table.Body>
                                    {searchResults.map((result, index) => (
                                        <Table.Row key={`document-${index}`}>
                                            <Table.Cell>{result.description}</Table.Cell>
                                            <Table.Cell>{result.indexed.toLocaleString()}</Table.Cell>
                                        </Table.Row>
                                    ))}
                                </Table.Body>
                            </Table>
                            {downloadState === states.PENDING && (
                                <ProgressBar status="Downloading documents..."></ProgressBar>
                            )}
                            <Button type="button" onClick={downloadAll} disabled={(downloadState === states.PENDING) || !hasCleanFiles}>
                                Download All Documents
                            </Button>
                            {downloadState === states.SUCCEEDED && (
                                <p>
                                    <strong>All documents have been successfully downloaded.</strong>
                                </p>
                            )}
                            <p>
                                Only use this option if you have a valid reason to permanently delete all available
                                documents for this patient. For example, if the retention period of these documents has
                                been reached.
                            </p>
                            <Link role="button" className="nhsuk-button" to={routes.SEARCH_RESULTS_DELETE}>
                                Delete All Documents
                            </Link>
                        </>
                    )}
                    {searchResults.length === 0 && (
                        <p>
                            <strong>There are no documents available for this patient.</strong>
                        </p>
                    )}
                </>
            )}
            {(submissionState === states.FAILED || submissionState === states.SUCCEEDED) && (
                <p>
                    <Link to="/home">Start Again</Link>
                </p>
            )}
        </>
    );
};

export default SearchResultsPage;
