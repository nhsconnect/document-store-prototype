import React from "react";
import { ButtonLink } from "nhsuk-react-components";

const NoValidOrgsPage = () => {
    return (
        <>
            <h1 role="heading">
                You do not have a valid organisation <br />
                to access this service
            </h1>
            <p>
                If you think you should have access to this service contact the{" "}
                <a
                    role="link"
                    href="https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks"
                    target="_blank"
                    rel="noreferrer"
                >
                    NHS National Service Desk
                </a>
            </p>

            <h2>Who can access this service</h2>
            <ul>
                <li>
                    General Practice Staff, you can upload an inactive patient&apos;s electronic health record and
                    attachments
                </li>
                <li>
                    PCSE, you can search for and download patient records where there has been an access request for an
                    inactive patient health record
                </li>
            </ul>
            <p>
                <ButtonLink href="/home">Return to start page</ButtonLink>
            </p>
        </>
    );
};
export default NoValidOrgsPage;
