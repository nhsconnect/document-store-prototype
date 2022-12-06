import React, {useEffect, useState} from 'react';
import {useForm} from 'react-hook-form';
import {Button, ErrorMessage, Fieldset, Input, Table} from 'nhsuk-react-components';
import {useNhsNumberProviderContext} from '../providers/NhsNumberProvider';
import {useNavigate} from 'react-router';
import BackButton from '../components/BackButton';
import {useFeatureToggle} from '../providers/FeatureToggleProvider';
import {downloadFile} from '../utils/utils';

const states = {
    INITIAL: 'initial',
    PENDING: 'pending',
    SUCCEEDED: 'succeeded',
    FAILED: 'failed',
};

export default function SearchResultsPage({client}) {
    const {register} = useForm();
    const [nhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();
    const isCIS2Enabled = useFeatureToggle('CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED');

    const [searchResults, setSearchResults] = useState([]);
    const [submissionState, setSubmissionState] = useState(states.INITIAL);
    const [downloadState, setDownloadState] = useState(states.INITIAL);

    const {ref: nhsNumberRef, ...nhsNumberProps} = register('nhsNumber');

    useEffect(() => {
        if (!nhsNumber) {
            navigate('/search/patient-trace');
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
    }, [client, nhsNumber, navigate, setSubmissionState, setSearchResults]);

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
    }

    function goToHome() {
        const homePagePath = isCIS2Enabled ? '/home' : '/';
        navigate(homePagePath);
    }

    return (
        <>
            <BackButton/>
            <Fieldset>
                <Fieldset.Legend headingLevel="h1" isPageHeading>Download and view a stored document</Fieldset.Legend>
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
                        <progress aria-label="Loading..."/>
                    </p>
                )}
            </Fieldset>
            {submissionState === states.FAILED && (
                <p>
                    Sorry, the search failed due to an internal error. Please
                    try again.
                </p>
            )}
            {submissionState === states.SUCCEEDED && (
                <>
                    {searchResults.length > 0 && (
                        <>
                            <p>You can choose to download all files for this patient</p>
                            <Button
                                type="button"
                                secondary
                                onClick={downloadAll}
                                disabled={downloadState === states.PENDING}>
                                {downloadState === states.PENDING ? 'Downloading...' : 'Download All'}
                            </Button>
                            {downloadState === states.FAILED &&
                                <ErrorMessage>Failed to download, please retry.</ErrorMessage>}
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
                                            <Table.Cell>
                                                {result.description}
                                            </Table.Cell>
                                            <Table.Cell>
                                                {result.indexed.toLocaleString()}
                                            </Table.Cell>
                                        </Table.Row>
                                    ))}
                                </Table.Body>
                            </Table>
                        </>
                    )}
                    {searchResults.length === 0 && <p>No record found</p>}
                </>
            )}
            {(submissionState === states.FAILED || submissionState === states.SUCCEEDED) &&
                <Button onClick={goToHome}>Start Again</Button>}
        </>
    );
}
