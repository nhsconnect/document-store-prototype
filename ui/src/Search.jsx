import React, {useState} from "react";
import {Input, Button, Table} from "nhsuk-react-components"

function Search({ apiClient }) {
    const [searchTerm, setSearchTerm] = useState("");
    const [searchResults, setSearchResults] = useState([]);

    const handleSubmit = async () => {
        const results = await apiClient.findByNhsNumber(searchTerm);
        setSearchResults(results);
    };

    return (
        <div>
            <div>
                <Input id={"nhs-number-input"} value={searchTerm} label="Find by NHS number"
                       onChange={event => setSearchTerm(event.target.value)}/>
                <Button onClick={handleSubmit}>
                    Search
                </Button>
            </div>
            {searchResults.length > 0 && (
                <Table caption="Documents">
                    <Table.Head>
                        <Table.Row>
                            <Table.Cell>Description</Table.Cell>
                            <Table.Cell>Type</Table.Cell>
                            <Table.Cell/>
                        </Table.Row>
                    </Table.Head>

                    <Table.Body>
                        {searchResults.map((result, index) => (
                            <Table.Row>
                            <Table.Cell>{result.description}</Table.Cell>
                                <Table.Cell>{result.type}</Table.Cell>
                                <Table.Cell><a href={result.url}>Link</a></Table.Cell>
                            </Table.Row>
                        ))}
                    </Table.Body>
                </Table>
            )}
        </div>
    );
}

export default Search;