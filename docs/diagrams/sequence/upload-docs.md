# Upload Docs

The sequence diagram below illustrate the interactions that occur when a user uploads docs.

The diagram assumes that AWS Amplify has served the React web app; the user is logged in; has the required
permissions to upload docs; and has found the patient they want to upload docs for (where the sequence begins).

```mermaid
sequenceDiagram
    actor GP Practice/PCSE User
    GP Practice/PCSE User ->> React Web App: Selects docs to upload
    activate React Web App
    React Web App ->> GP Practice/PCSE User: Displays doc upload progress
    React Web App ->> API Gateway: POST /DocumentReference
    activate API Gateway
    API Gateway ->> Lambda: Invokes CreateDocumentReferenceHandler
    activate Lambda
    Lambda ->> S3: generatePresignedUrl()
    activate S3
    S3 ->> Lambda: URL
    deactivate S3
    Lambda -->> DynamoDB: save()
    Note over Lambda, DynamoDB: DocumentReferenceMetadata table
    Lambda -->> SQS: sendMessage()
    Note over Lambda, SQS: <env>-sensitive-audit queue
    activate SQS
    SQS -->> Lambda: SendMessageResponse
    deactivate SQS
    Lambda ->> API Gateway: 201 NHSDocumentReference
    deactivate Lambda
    API Gateway ->> React Web App: 201 NHSDocumentReference
    API Gateway -->> S3: PUT s3Url
    activate S3
    S3 -->> API Gateway: 200
    deactivate S3
    deactivate API Gateway
    React Web App ->> GP Practice/PCSE User: Displays uploaded doc
    deactivate React Web App
    loop Every 5 mins
        Splunk ->> SQS: Polls for audit messages
        Note over Splunk, SQS: <env>-sensitive-audit queue
    end
```
