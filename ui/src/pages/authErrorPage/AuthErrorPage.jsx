import { ButtonLink } from "nhsuk-react-components";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";

const AuthErrorPage = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    const timeoutString = "15 minutes";

    return (
        <>
            <h1 role="heading">You have been logged out</h1>
            <p>
                For security reasons, you&apos;re automatically logged out if you have not used the service for{" "}
                {timeoutString}.
            </p>
            <p>If you were entering information, it has not been saved and you will need to re-enter it.</p>
            <p>
                If the issue persists please contact the{" "}
                <a
                    role="link"
                    href="https://digital.nhs.uk/about-nhs-digital/contact-us#nhs-digital-service-desks"
                    target="_blank"
                    rel="noreferrer"
                >
                    NHS National Service Desk
                </a>
                .
            </p>
            <ButtonLink href={`${baseAPIUrl}/Auth/Login`}>Log In</ButtonLink>
        </>
    );
};
export default AuthErrorPage;
