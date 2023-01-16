import React from "react";
import {ButtonLink} from "nhsuk-react-components";

export default function StartPage() {
    return (
        <>
            <h1>Inactive Patient Record Administration</h1>
            <p>
                When a patient is inactive, NHS England via Primary Care Support England are responsible for
                administration of their Electronic health record (EHR) and attachments until they register at their next
                GP Practice.
            </p>
            <p>
                General Practice Staff should use this service to upload an inactive patient&apos;s electronic health
                record and attachments.
            </p>
            <p>
                PCSE should use this service to search for and download patient records where there has been an access
                request for an inactive patient health record.
            </p>
            <h2 className="nhsuk-heading-m">Before you start</h2>
            <p>You can only use this service if you have a valid CIS2 account</p>
            <ButtonLink href="/home">Start now</ButtonLink>
        </>
    );
}
