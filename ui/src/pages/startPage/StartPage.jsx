import React from "react";
import { ButtonLink } from "nhsuk-react-components";
import { useBaseAPIUrl, useFeatureToggle } from "../../providers/configProvider/ConfigProvider";

const StartPage = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

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
            <p>
                If there is an issue with the service please contact the{" "}
                <a
                    href="https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks"
                    target="_blank"
                    rel="noreferrer"
                >
                    NHS National Service Desk
                </a>
                .
            </p>
            <h2>Before You Start</h2>
            <p>You can only use this service if you have a valid NHS smartcard.</p>
            <ButtonLink href={isOIDCAuthActive ? "/home" : `${baseAPIUrl}/Auth/Login`}>Start now</ButtonLink>
        </>
    );
};

export default StartPage;
