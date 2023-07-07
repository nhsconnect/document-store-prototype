import React, { useEffect, useState } from "react";
import { Button, Table, ErrorMessage, ErrorSummary } from "nhsuk-react-components";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";
import { downloadFile } from "../../utils/utils";
import PatientSummary from "../../components/patientSummary/PatientSummary";
import ProgressBar from "../../components/progressBar/ProgressBar";
import ServiceError from "../../components/serviceError/ServiceError";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import SpinnerButton from "../../components/spinnerButton/SpinnerButton";

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
    const [numberOfCleanFiles, setNumberOfCleanFiles] = useState(0);
    const [session, setSession] = useSessionContext();

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
                setNumberOfCleanFiles(results.filter((doc) => doc.virusScanResult === "Clean").length);
                setSubmissionState(states.SUCCEEDED);
            } catch (e) {
                if (e.response?.status === 403) {
                    setSession({
                        ...session,
                        isLoggedIn: false,
                    });
                    navigate(routes.ROOT);
                }
                setSubmissionState(states.FAILED);
            }
        };

        void search();
    }, [documentStore, patientDetails, navigate, setSubmissionState, setSearchResults, setSession, session]);

    const downloadAll = async () => {
        setDownloadState(states.PENDING);
        try {
            const preSignedUrl = await documentStore.getPresignedUrlForZip(patientDetails.nhsNumber);

            downloadFile(preSignedUrl, `patient-record-${patientDetails.nhsNumber}`);

            setDownloadState(states.SUCCEEDED);
        } catch (e) {
            if (e.response?.status === 403) {
                setSession({
                    ...session,
                    isLoggedIn: false,
                });
                navigate(routes.ROOT);
            }
            setDownloadState(states.FAILED);
        }
    };

    const docsAvailableTableCaption = <h3 style={{ fontSize: 32 }}>List of documents available</h3>;

    return (
        <>
            <h1>Download electronic health records and attachments</h1>
            {(submissionState === states.FAILED || downloadState === states.FAILED) && <ServiceError />}
            <PatientSummary patientDetails={patientDetails} />
            {submissionState === states.PENDING && <ProgressBar status="Loading..."></ProgressBar>}
            {submissionState === states.SUCCEEDED && (
                <>
                    {searchResults.length > 0 && (
                        <>
                            {numberOfCleanFiles < searchResults.length && (
                                <>
                                    <ErrorSummary>
                                        <ErrorSummary.Title>There is a problem</ErrorSummary.Title>
                                        <ErrorSummary.Body>
                                            <ErrorMessage>
                                                Some files are not available for download due to an issue
                                            </ErrorMessage>
                                            <ErrorMessage>
                                                Take a screenshot of the list and contact GP Practice to access the
                                                files
                                            </ErrorMessage>
                                        </ErrorSummary.Body>
                                    </ErrorSummary>
                                    <Table
                                        className={"nhsuk-form-group--error"}
                                        caption="List of documents not available"
                                    >
                                        <Table.Head>
                                            <Table.Row>
                                                <Table.Cell>Filename</Table.Cell>
                                            </Table.Row>
                                        </Table.Head>
                                        <Table.Body>
                                            {searchResults
                                                .filter((result) => result.virusScanResult !== "Clean")
                                                .map((result, index) => (
                                                    <Table.Row key={`document-${index}-error`}>
                                                        <Table.Cell>
                                                            <ErrorMessage>{result.description}</ErrorMessage>
                                                        </Table.Cell>
                                                    </Table.Row>
                                                ))}
                                        </Table.Body>
                                    </Table>
                                </>
                            )}
                            {numberOfCleanFiles > 0 && (
                                <Table caption={docsAvailableTableCaption}>
                                    <Table.Head>
                                        <Table.Row>
                                            <Table.Cell>Filename</Table.Cell>
                                            <Table.Cell>Uploaded At</Table.Cell>
                                        </Table.Row>
                                    </Table.Head>
                                    <Table.Body>
                                        {searchResults
                                            .filter((result) => result.virusScanResult === "Clean")
                                            .map((result, index) => (
                                                <Table.Row key={`document-${index}`}>
                                                    <Table.Cell>{result.description}</Table.Cell>
                                                    <Table.Cell>{result.indexed.toLocaleString()}</Table.Cell>
                                                </Table.Row>
                                            ))}
                                    </Table.Body>
                                </Table>
                            )}
                            <p>
                                Only permanently delete all documents for this patient if you have a valid reason to.
                                For example, if the retention period of these documents has been reached.
                            </p>
                            <>
                                {downloadState === states.PENDING ? (
                                    <SpinnerButton status="Downloading documents" />
                                ) : (
                                    <Button
                                        type="button"
                                        style={{ marginRight: 72 }}
                                        onClick={downloadAll}
                                        disabled={numberOfCleanFiles < 1}
                                    >
                                        Download All Documents
                                    </Button>
                                )}
                                <Button secondary role="button" href={routes.SEARCH_RESULTS_DELETE}>
                                    Delete All Documents
                                </Button>
                            </>
                            {downloadState === states.SUCCEEDED && (
                                <p>
                                    <strong>All documents have been successfully downloaded.</strong>
                                </p>
                            )}
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
