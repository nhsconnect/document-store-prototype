import React from "react";

const Spinner = ({ status }) => {
    return (
        <div className="nhsuk-loader" role="Spinner" aria-label={status}>
            <span className="nhsuk-loader__text" role="status">
                {status}
            </span>
            <span className="spinner-blue"></span>
        </div>
    );
};

export default Spinner;
