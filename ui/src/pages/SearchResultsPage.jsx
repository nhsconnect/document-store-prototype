import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Button, ErrorMessage, Fieldset, Input, Table } from "nhsuk-react-components";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import { useNavigate } from "react-router";
import BackButton from "../components/BackButton";
import useApi from "../apiClients/useApi";
import { downloadFile } from "../utils/utils";

const states = {
    INITIAL: "initial",
    PENDING: "pending",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const SearchResultsPage = () => {
    const client = useApi();
    const { register } = useForm();
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
    const [searchResults, setSearchResults] = useState([]);
    const [submissionState, setSubmissionState] = useState(states.INITIAL);
    const [downloadState, setDownloadState] = useState(states.INITIAL);
    const [nhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (!nhsNumber) {
            navigate("/search/patient-trace");
            return;
        }
        const search = async () => {
            setSubmissionState(states.PENDING);
            setSearchResults([]);
            try {
                const results = await client.findByNhsNumber(nhsNumber);
                results.sort((a, b) => (a.indexed < b.indexed ? 1 : -1));
                setSearchResults(results);
                setSubmissionState(states.SUCCEEDED);
            } catch (error) {
                setSubmissionState(states.FAILED);
            }
        };
        void search();

        // Todo: Remove the suppression when we provide a client to the dependency array that remains stable between renders
        // eslint-disable-next-line
    }, [nhsNumber, navigate, setSubmissionState, setSearchResults]);

    const downloadAll = async () => {
        setDownloadState(states.PENDING);
        try {
            const preSignedUrl = await client.getPresignedUrlForZip(nhsNumber);

            downloadFile(preSignedUrl, `patient-record-${nhsNumber}`);

            setDownloadState(states.SUCCEEDED);
        } catch (e) {
            setDownloadState(states.FAILED);
            console.error(e);
        }
    };

    const goToDeleteDocumentsConfirmationPage = () => {
        navigate("/search/results/delete-documents-confirmation");
    };

    return (
        <>
            <BackButton />
            <Fieldset>
                <Fieldset.Legend headingLevel="h1" isPageHeading>
                    Download and view a stored document
                </Fieldset.Legend>
                <Input
                    id="nhs-number-input"
                    name="nhsNumber"
                    label="Find by NHS number"
                    {...nhsNumberProps}
                    inputRef={nhsNumberRef}
                    value={nhsNumber}
                    readOnly
                />
                {submissionState === states.PENDING && (
                    <p>
                        <progress aria-label="Loading..." />
                    </p>
                )}
            </Fieldset>
            {submissionState === states.FAILED && (
                <p>Sorry, the search failed due to an internal error. Please try again.</p>
            )}
            {submissionState === states.SUCCEEDED && (
                <>
                    {searchResults.length > 0 && (
                        <>
                            <p>You can choose to download all files for this patient</p>
                            <Button type="button" onClick={downloadAll} disabled={downloadState === states.PENDING}>
                                {downloadState === states.PENDING
                                    ? "Downloading All Documents..."
                                    : "Download All Documents"}
                            </Button>
                            {downloadState === states.FAILED && (
                                <ErrorMessage>Failed to download, please retry.</ErrorMessage>
                            )}
                            <Table caption="List of documents available to download">
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
                            <p>
                                Only use this option if you have a valid reason to permanently delete all available
                                documents for this patient. For example, if the retention period of these documents has
                                been reached
                            </p>

                            <Button type="button" secondary onClick={goToDeleteDocumentsConfirmationPage}>
                                Delete All Documents
                            </Button>
                        </>
                    )}
                    {searchResults.length === 0 && <p>No record found</p>}
                </>
            )}

            <>
                {(submissionState === states.FAILED || submissionState === states.SUCCEEDED) && (
                    <p>
                        <a className="govuk-link" href="/home">
                            Start Again
                        </a>
                    </p>
                )}
            </>
        </>
    );
};

export default SearchResultsPage;
