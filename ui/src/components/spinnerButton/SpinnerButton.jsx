import React from "react";
import { Button } from "nhsuk-react-components";
import "./styles/spinner.scss";

const SpinnerButton = ({ status, disabled }) => {
    return (
        <Button aria-label="SpinnerButton" className="spinner-button" role="SpinnerButton" disabled={disabled}>
            <div className="spinner"></div>
            <div role="status">{status}</div>
        </Button>
    );
};

export default SpinnerButton;
