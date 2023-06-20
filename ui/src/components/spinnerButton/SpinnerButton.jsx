import React from "react";
import { Button } from "nhsuk-react-components";

const SpinnerButton = ({ status }) => {
    const buttonStyle = { display: "flex" };
    const spinnerStyle = {};

    return (
        <>
            <Button aria-label={status} style={buttonStyle} role="SpinnerButton">
                <div className="spinner-button" style={spinnerStyle}></div>
                <div role="status" className="nhsuk-loader__text">
                    {status}
                </div>
            </Button>
        </>
    );
};

export default SpinnerButton;
