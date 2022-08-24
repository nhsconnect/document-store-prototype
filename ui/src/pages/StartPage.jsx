import React from "react";
import { ButtonLink } from "nhsuk-react-components";

export default () => {
    return (
        <>
            <h3>Document Store</h3>
            <p>Use this service to:</p>
            <ul>
                <li>upload patient digital records</li>
                <li>download and view patient digital records</li>
            </ul>
            <h4>Before you start</h4>
            <p>
                You can only use this service if you have a valid CIS2 account
            </p>
            <ButtonLink href={"/home"}>Start now</ButtonLink>
        </>
    );
};
