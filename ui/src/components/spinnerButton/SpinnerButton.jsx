import React from "react";
import { Button } from "nhsuk-react-components";

const SpinnerButton = ({ status }) => {
    const buttonStyle = { display: "flex" };

    return (
        <>
            <Button style={buttonStyle} role="SpinnerButton">
                <div className="spinner-button"></div>
                <div role="status" className="nhsuk-loader__text" aria-label={status}>
                    {status}
                </div>
            </Button>
        </>
    );
};

export default SpinnerButton;
