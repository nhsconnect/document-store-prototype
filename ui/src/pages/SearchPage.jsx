import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { Input, Button, Table } from "nhsuk-react-components";

const states = {
    INITIAL: "initial",
    SEARCHING: "searching",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
};

const SearchPage = ({ client }) => {
    const { register, handleSubmit } = useForm();
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
    const [searchResults, setSearchResults] = useState([]);
    const [submissionState, setSubmissionState] = useState(states.INITIAL);

    const doSubmit = async (data) => {
        setSubmissionState(states.SEARCHING);
        setSearchResults([]);
        try {
            const results = await client.findByNhsNumber(data.nhsNumber);
            setSearchResults(results);
            setSubmissionState(states.SUCCEEDED);
        } catch (error) {
            setSubmissionState(states.FAILED);
        }
    };

    return (
        <div>
            <div>
                <form onSubmit={handleSubmit(doSubmit)}>
                    <Input
                        id={"nhs-number-input"}
                        name="nhsNumber"
                        label="Find by NHS number"
                        {...nhsNumberProps}
                        inputRef={nhsNumberRef}
                    />
                    <Button>Search</Button>
                    {submissionState === states.SEARCHING && (
                        <p>
                            <progress aria-label={"Loading..."}></progress>
                        </p>
                    )}
                </form>
            </div>
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
                                </Table.Row>
                            </Table.Head>

                            <Table.Body>
                                {searchResults.map((result, index) => (
                                    <Table.Row>
                                        <Table.Cell>
                                            <a href={result.url}>
                                                {result.description}
                                            </a>
                                        </Table.Cell>
                                        <Table.Cell>{result.type}</Table.Cell>
                                        <Table.Cell>
                                            {result.indexed.toLocaleString()}
                                        </Table.Cell>
                                    </Table.Row>
                                ))}
                            </Table.Body>
                        </Table>
                    )}

                    {searchResults.length === 0 && <p>No record found</p>}
                </>
            )}
        </div>
    );
};

export default SearchPage;
