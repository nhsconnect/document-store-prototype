import React, {useEffect, useState} from "react";
import {useForm} from "react-hook-form";
import {Button, ErrorMessage, Fieldset, Input, Table} from "nhsuk-react-components";
import {useNhsNumberProviderContext} from "../providers/NhsNumberProvider";
import {useNavigate} from "react-router";
import BackButton from "../components/BackButton";
import {setUrlHostToLocalHost} from "../utils/utils";

const states = {
    INITIAL: "initial",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const alignMiddle = {
    verticalAlign: 'middle'
}

function Document({client, documentData, downloadError}) {
    const [disabled, setDisabled] = useState(false);

    async function handleClick(id, fileName) {
        if (id) {
            try {
                downloadError(false);
                setDisabled(true);
                const attachment = await client.getPresignedUrl(id);
                fetch(setUrlHostToLocalHost(attachment.url))
                    .then(res => res.blob())
                    .then(res => {
                        const aElement = document.createElement('a');
                        aElement.setAttribute('download', fileName);
                        const href = URL.createObjectURL(res);
                        aElement.href = href;
                        aElement.click();
                        URL.revokeObjectURL(href);
                    });
            } catch (e) {
                downloadError(true);
            }
            setDisabled(false);
        }
    }

    return (
        <Table.Row>
            <Table.Cell style={alignMiddle}>
                {documentData.description}
            </Table.Cell>
            <Table.Cell style={alignMiddle}>
                {documentData.indexed.toLocaleString()}
            </Table.Cell>
            <Table.Cell style={{...alignMiddle, width: 200}}>
                <Button style={{marginBottom:0}}
                        secondary
                        disabled={disabled}
                        onClick={() => handleClick(documentData.id, documentData.description)}>
                    {disabled ? "Downloading..." : "Download"}
                </Button>
            </Table.Cell>
        </Table.Row>
    );
}

const SearchResultsPage = ({client}) => {
    const {register} = useForm();
    const {ref: nhsNumberRef, ...nhsNumberProps} = register("nhsNumber");
    const [searchResults, setSearchResults] = useState([]);
    const [downloadError, setDownloadError] = useState(false);
    const [submissionState, setSubmissionState] = useState(states.INITIAL);
    const [nhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (!nhsNumber) {
            navigate("/search/patient-trace");
            return;
        }
        const search = async () => {
            setSubmissionState(states.SEARCHING);
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

    const downloadAll = async() => {
        const uri = await client.getPresignedUrlForZip(nhsNumber);
        console.log(uri);
    }

    return (
        <>
            <BackButton/>
            <Fieldset>
                <Fieldset.Legend headingLevel={'h1'} isPageHeading>Download and view a stored document</Fieldset.Legend>
                <Input
                    id={"nhs-number-input"}
                    name="nhsNumber"
                    label="Find by NHS number"
                    {...nhsNumberProps}
                    inputRef={nhsNumberRef}
                    value={nhsNumber}
                    readOnly
                />
                {submissionState === states.SEARCHING && (
                    <p>
                        <progress aria-label={"Loading..."}></progress>
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
                            <Button onClick={downloadAll}>Download All</Button>
                            {downloadError && <ErrorMessage>Failed to download, please retry.</ErrorMessage>}
                            <Table caption="Documents">
                                <Table.Head>
                                    <Table.Row>
                                        <Table.Cell>Filename</Table.Cell>
                                        <Table.Cell>Uploaded At</Table.Cell>
                                        <Table.Cell>Download</Table.Cell>
                                    </Table.Row>
                                </Table.Head>

                                <Table.Body>
                                    {searchResults.map((result) => (
                                        <Document key={result.id} client={client} documentData={result}
                                                  downloadError={(error) => setDownloadError(error)}/>
                                    ))}
                                </Table.Body>
                            </Table>
                        </>
                    )}

                    {searchResults.length === 0 && <p>No record found</p>}
                </>
            )}
        </>
    );
};

export default SearchResultsPage;
