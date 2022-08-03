import Search from "../components/Search";
import React from "react";

const SearchPage = ({client}) => {
    return (
        <>
            <h2>View Stored Patient Record</h2>
            <div>
                <Search apiClient={client} />
            </div>
        </>
    )
}

export default SearchPage