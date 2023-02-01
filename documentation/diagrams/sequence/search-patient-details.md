# Search Patient Details

The sequence diagram below illustrates the interactions that occur when a user searches for a patient.

This diagram assumes that the user is logged in and has the required permissions to search for a valid NHS number.

```mermaid
sequenceDiagram
    actor GP Practice/PCSE User
    GP Practice/PCSE User->>AWS Amplify: Selects upload or download doc option
    activate AWS Amplify
    AWS Amplify-->>GP Practice/PCSE User: Navigates to search patient page
    deactivate AWS Amplify
    GP Practice/PCSE User->>AWS Amplify: Searches for patient
    activate AWS Amplify
    AWS Amplify->>API Gateway: GET /PatientDetails
    activate API Gateway
    API Gateway->>Lambda: Invokes SearchPatientDetailsHandler
    activate Lambda
    Lambda->>PDS FHIR: GET Patient/<nhsNumber>
    activate PDS FHIR
    PDS FHIR-->>Lambda: 200 Patient
    deactivate PDS FHIR
    Lambda-->>SQS: sendMessage(auditMessageRequest)
    activate SQS
    SQS-->>Lambda: Responds with successful SendMessageResponse
    deactivate SQS
    Lambda-->>API Gateway: 200 PatientDetails
    deactivate Lambda
    API Gateway-->>AWS Amplify: 200 PatientDetails
    deactivate API Gateway
    AWS Amplify-->>GP Practice/PCSE User: Displays patient details
    deactivate AWS Amplify
```
