import React from "react";

const SimpleProgressBar = ({ status }) => {
    return (
        <>
            <progress aria-label={status} role={"progressbar"} />
            <p role="status">{status}</p>
        </>
    );
};

export default SimpleProgressBar;
