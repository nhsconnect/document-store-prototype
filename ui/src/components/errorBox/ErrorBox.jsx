import { ErrorSummary } from "nhsuk-react-components";

const ErrorBox = ({ errorBoxSummaryId, errorInputLink, messageTitle, messageBody, messageLinkBody }) => {
    return (
        <ErrorSummary aria-labelledby={errorBoxSummaryId} role="alert" tabIndex={-1}>
            <ErrorSummary.Title id={errorBoxSummaryId}>{messageTitle}</ErrorSummary.Title>
            <ErrorSummary.Body>
                <ErrorSummary.List>
                    <p>{messageBody}</p>
                    <ErrorSummary.Item href={errorInputLink}>
                        <p>{messageLinkBody}</p>
                    </ErrorSummary.Item>
                </ErrorSummary.List>
            </ErrorSummary.Body>
        </ErrorSummary>
    );
};

export default ErrorBox;
