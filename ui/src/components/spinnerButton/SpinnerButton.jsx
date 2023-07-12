import React from "react";
import { Button } from "nhsuk-react-components";

const SpinnerButton = ({ status }) => {
    const buttonStyle = { display: "flex" };

    return (
        <Button aria-label="SpinnerButton" style={buttonStyle} role="SpinnerButton">
            <div className="spinner-button"></div>
            <div role="status">{status}</div>
        </Button>
    );
};

export default SpinnerButton;
