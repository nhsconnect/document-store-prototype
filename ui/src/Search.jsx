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
                <label htmlFor={"nhs-number-input"}>
                    Find by NHS number
                </label>
                <Input id={"nhs-number-input"} value={searchTerm}
                       onChange={event => setSearchTerm(event.target.value)}/>
                <Button onClick={handleSubmit}>
                    Search
                </Button>
            </div>
            {searchResults.length > 0 && (
                <Table caption="Documents">
                    <Table.Head>
                        <Table.Row>
                            <Table.Cell>ID</Table.Cell>
                            <Table.Cell>Description</Table.Cell>
                            <Table.Cell></Table.Cell>
                        </Table.Row>
                    </Table.Head>

                    <Table.Body>
                        {searchResults.map((result, index) => <Table.Row><Table.Cell>{result.id}</Table.Cell> <Table.Cell>{result.description}</Table.Cell><Table.Cell><a href={result.url}>Link</a></Table.Cell></Table.Row>)}
                    </Table.Body>
                </Table>
            )}
        </div>
    );
}

export default Search;