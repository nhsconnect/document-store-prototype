import React, { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import {ButtonLink, Fieldset, Input, Table} from "nhsuk-react-components";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import { useNavigate } from "react-router";
import BackButton from "../components/BackButton";

const states = {
    INITIAL: "initial",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const SearchResultsPage = ({ client }) => {
    const { register } = useForm();
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
    const [searchResults, setSearchResults] = useState([]);
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

    async function handleClick(id) {
        if (id) {
            const url = await client.getPresignedUrl(id);
            url && window.open(url);
        }
    }

    return (
        <>
            <BackButton />
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
                        <Table caption="Documents">
                            <Table.Head>
                                <Table.Row>
                                    <Table.Cell>Description</Table.Cell>
                                    <Table.Cell>Type</Table.Cell>
                                    <Table.Cell>Uploaded At</Table.Cell>
                                    <Table.Cell>View And Download</Table.Cell>
                                </Table.Row>
                            </Table.Head>

                            <Table.Body>
                                {searchResults.map((result) => (
                                    <Table.Row key={result.id}>
                                        <Table.Cell>
                                                {result.description}
                                        </Table.Cell>
                                        <Table.Cell>{result.type}</Table.Cell>
                                        <Table.Cell>
                                            {result.indexed.toLocaleString()}
                                        </Table.Cell>
                                      <Table.Cell>
                                        <ButtonLink secondary onClick={() => handleClick(result.id)}>Download</ButtonLink>
                                      </Table.Cell>
                                    </Table.Row>
                                ))}
                            </Table.Body>
                        </Table>
                    )}

                    {searchResults.length === 0 && <p>No record found</p>}
                </>
            )}
        </>
    );
};

export default SearchResultsPage;
