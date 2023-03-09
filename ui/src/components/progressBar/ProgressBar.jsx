import React from "react";

const ProgressBar = ({ status }) => {
    return (
        <>
            <progress aria-label={status} role="progressbar" />
            <p role="status">{status}</p>
        </>
    );
};

export default ProgressBar;
