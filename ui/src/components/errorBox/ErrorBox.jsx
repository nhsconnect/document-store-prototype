import { ErrorSummary } from "nhsuk-react-components";

const ErrorBox = ({ messageTitle, messageBody }) => {
    const errorBoxSummaryId = "error-box-summary";
    const errorInputLink = "#nhs-number-input";

    return (
        <ErrorSummary aria-labelledby={errorBoxSummaryId} role="alert" tabIndex={-1}>
            <ErrorSummary.Title id={errorBoxSummaryId}>{messageTitle}</ErrorSummary.Title>

            <ErrorSummary.Body>
                <ErrorSummary.List>
                    <ErrorSummary.Item href={errorInputLink}>
                        <p>{messageBody}</p>
                    </ErrorSummary.Item>
                </ErrorSummary.List>
            </ErrorSummary.Body>
        </ErrorSummary>
    );
};

export default ErrorBox;
