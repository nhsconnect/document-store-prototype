import React from "react";

const Spinner = ({ status }) => {
    const spinnerStyle = {
        boxSizing: "border-box",
        margin: "auto",
        width: "120px",
        height: "120px",
        borderRadius: "50%",
    };
    return (
        <div className="nhsuk-loader" role="Spinner" aria-label={status}>
            <span className="spinner-blue" style={spinnerStyle}></span>
            <span className="nhsuk-loader__text" role="status">
                {status}
            </span>
        </div>
    );
};

export default Spinner;
