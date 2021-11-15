import React, {useState} from "react";

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
                <input id={"nhs-number-input"} value={searchTerm}
                       onChange={event => setSearchTerm(event.target.value)}/>
                <button onClick={handleSubmit}>
                    Search
                </button>
            </div>
            {searchResults.length > 0 && (
                <div>
                    Results
                    <ul>
                        {searchResults.map((result, index) => <li key={index}>{result.description}</li>)}
                    </ul>
                </div>
            )}
        </div>
    );
}

export default Search;