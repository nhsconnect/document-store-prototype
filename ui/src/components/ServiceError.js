import { ErrorSummary } from "nhsuk-react-components";

const ServiceError = () => {
    return (
        <ErrorSummary aria-labelledby="service-error-summary" role="alert" tabIndex={-1}>
            <ErrorSummary.Title id="service-error-summary-title">
                Sorry, there is a problem with the service
            </ErrorSummary.Title>
            <ErrorSummary.Body>
                <p>Try again later.</p>
                <p>
                    If there is an issue with the service please contact the{" "}
                    <a href={"https://digital.nhs.uk/about-nhs-digital/contact-us"}>
                        NHS Digital National Service Desk
                    </a>
                    .
                </p>
            </ErrorSummary.Body>
        </ErrorSummary>
    );
};

export default ServiceError;
