# Search Patient Details

The sequence diagram below illustrates the interactions that occur when a user searches for a patient.

This diagram assumes that AWS Amplify has served the React web app; the user is logged in; and has the required
permissions to search for a valid NHS number.

```mermaid
sequenceDiagram
    actor GP Practice/PCSE User
    GP Practice/PCSE User->>React Web App: Selects upload or download doc option
    activate React Web App
    React Web App-->>GP Practice/PCSE User: Navigates to search patient page
    deactivate React Web App
    GP Practice/PCSE User->>React Web App: Searches for patient
    activate React Web App
    React Web App->>API Gateway: GET /PatientDetails
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
    API Gateway-->>React Web App: 200 PatientDetails
    deactivate API Gateway
    React Web App-->>GP Practice/PCSE User: Displays patient details
    loop Every 5 mins
        Splunk-->>SQS: Polls for audit messages
    end
    deactivate React Web App
```
