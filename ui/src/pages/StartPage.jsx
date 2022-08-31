import React from "react";
import { ButtonLink } from "nhsuk-react-components";

const StartPage = () => {
    return (
        <>
            <h1>Document Store</h1>
            <p>Use this service to:</p>
            <ul>
                <li>Upload patient digital records</li>
                <li>Download and view patient digital records</li>
            </ul>
            <h2 className="nhsuk-heading-m">Before you start</h2>
            <p>
                You can only use this service if you have a valid CIS2 account
            </p>
            <ButtonLink href={"/home"}>Start now</ButtonLink>
        </>
    );
};

export default StartPage;
