import React, {useState} from "react";

function Search() {
    const [searchTerm, setSearchTerm] = useState("");
    const [searchResults, setSearchResults] = useState([]);

    const handleSubmit = () => {
        setSearchResults([{"description": "this is a file", "type": "123456", "url": ""}]);
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
                    <ul>
                        {searchResults.map((result, index) => <li key={index}>{result.description}</li>)}
                    </ul>
                </div>
            )}
        </div>
    );
}

export default Search;