import React, { useEffect } from "react";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import { ButtonLink } from "nhsuk-react-components";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";

function InactivityErrorPage() {
    const [, , deleteSession] = useSessionContext();
    useEffect(() => {
        deleteSession();
    }, [deleteSession]);
    const timeoutString = "15 minutes";
    const baseUrl = useBaseAPIUrl("doc-store-api");

    return (
        <>
            <h1 role="heading">You have been logged out</h1>
            <p>
                For security reasons, you&apos;re automatically logged out if you have not used the service for{" "}
                {timeoutString}.
            </p>
            <p>If you were entering information, it has not been saved and you will need to re-enter it.</p>
            <ButtonLink href={`${baseUrl}/Auth/Login`}>Log In</ButtonLink>
        </>
    );
}

export default InactivityErrorPage;
