import React from "react";

const Spinner = ({ status }) => {
    return (
        <div className="nhsuk-loader" role="Spinner" aria-label="spinner">
            <span className="nhsuk-loader__text" role="status" aria-label={status}>
                {status}
            </span>
            <span className="spinner-blue"></span>
        </div>
    );
};

export default Spinner;
