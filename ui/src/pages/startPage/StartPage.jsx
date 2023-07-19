import React, { useState } from "react";
import { ButtonLink } from "nhsuk-react-components";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import Spinner from "../../components/spinner/Spinner";

const StartPage = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const [loading, setLoading] = useState(false);
    return !loading ? (
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
            <ButtonLink
                onClick={(e) => {
                    e.preventDefault();
                    setLoading(true);
                    window.location.replace(`${baseAPIUrl}/Auth/Login`);
                }}
            >
                Start now
            </ButtonLink>
        </>
    ) : (
        <Spinner status="Logging in..." />
    );
};

export default StartPage;
