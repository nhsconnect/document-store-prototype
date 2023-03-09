import { ErrorSummary } from "nhsuk-react-components";

const ServiceError = ({ message }) => {
    const serviceErrorSummaryId = "service-error-summary";

    return (
        <ErrorSummary aria-labelledby={serviceErrorSummaryId} role="alert" tabIndex={-1}>
            <ErrorSummary.Title id={serviceErrorSummaryId}>
                Sorry, the service is currently unavailable.
            </ErrorSummary.Title>
            <ErrorSummary.Body>
                <p>{message || "Please try again later."}</p>
                <p>
                    Please check your internet connection. If the issue persists please contact the{" "}
                    <a href="https://digital.nhs.uk/about-nhs-digital/contact-us" target="_blank" rel="noreferrer">
                        NHS Digital National Service Desk
                    </a>
                    .
                </p>
            </ErrorSummary.Body>
        </ErrorSummary>
    );
};

export default ServiceError;
