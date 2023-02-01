import { ErrorSummary } from "nhsuk-react-components";

const ServiceError = ({ message }) => {
    return (
        <ErrorSummary aria-labelledby="service-error-summary" role="alert" tabIndex={-1}>
            <ErrorSummary.Title id="service-error-summary-title">
                Sorry, the service is currently unavailable.
            </ErrorSummary.Title>
            <ErrorSummary.Body>
                <p>{message || "Please try again later."}</p>
                <p>
                    Please check your internet connection. If the issue persists please contact the{" "}
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
