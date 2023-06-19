import React from "react";
import { Button } from "nhsuk-react-components";

const SpinnerButton = ({ status }) => {
    const buttonStyle = { display: "flex" };
    const spinnerStyle = {
        boxSizing: "border-box",
        margin: "5px",
        marginRight: "10px",
        width: "20px",
        height: "20px",
        borderRadius: "50%",
    };

    return (
        <>
            <Button aria-label={status} style={buttonStyle} role="SpinnerButton">
                <div className="spinner-white" style={spinnerStyle}></div>
                <div role="status" className="nhsuk-loader__text">
                    {status}
                </div>
            </Button>
        </>
    );
};

export default SpinnerButton;
