# Document Upload

The sequence diagrams below illustrate the interactions that occur when a user uploads a doc that produces a positive
result when scanned for a virus.

The diagrams assume that AWS Amplify has served the React web app; the user is logged in; and has the required
permissions to upload a doc (where the sequences begin).

## Without Virus Scanning (As-Is)

```mermaid
sequenceDiagram
    actor GP Practice/PCSE User
    GP Practice/PCSE User->>React Web App: Selects docs to upload
    activate React Web App
    React Web App-->>GP Practice/PCSE User: Displays doc upload progress
    React Web App->>API Gateway: POST /DocumentReference
    activate API Gateway
    API Gateway->>Lambda: Invokes CreateDocumentReferenceHandler
    activate Lambda
    Lambda->>S3: generatePresignedUrl(uploadRequest)
    activate S3
    S3-->>Lambda: presignedS3Url
    deactivate S3
    Lambda-->>DynamoDB: save(documentMetadata)
    Lambda-->>SQS: sendMessage(auditMessageRequest)
    activate SQS
    SQS-->>Lambda: Responds with successful SendMessageResponse
    deactivate SQS
    Lambda-->>API Gateway: 201 NHSDocumentReference
    deactivate Lambda
    API Gateway-->>React Web App: 201 NHSDocumentReference
    API Gateway-->>S3: PUT s3Url
    activate S3
    S3-->>API Gateway: 200
    deactivate S3
    deactivate API Gateway
    React Web App-->>GP Practice/PCSE User: Displays uploaded doc
    deactivate React Web App
        loop Every 5 mins
        Splunk-->>SQS: Polls for audit messages
    end
```

## With Virus Scanning (To-Be)

**🚧 TODO: Add virus scanning implementation to diagram 🚧**

```mermaid
sequenceDiagram
    actor GP Practice/PCSE User
    GP Practice/PCSE User->>React Web App: Selects docs to upload
    activate React Web App
    React Web App-->>GP Practice/PCSE User: Displays doc upload progress
    React Web App->>API Gateway: POST /DocumentReference
    activate API Gateway
    API Gateway->>Lambda: Invokes CreateDocumentReferenceHandler
    activate Lambda
    Lambda->>S3: generatePresignedUrl(uploadRequest)
    activate S3
    S3-->>Lambda: presignedS3Url
    deactivate S3
    Lambda-->>DynamoDB: save(documentMetadata)
    Lambda-->>SQS: sendMessage(auditMessageRequest)
    activate SQS
    SQS-->>Lambda: Responds with successful SendMessageResponse
    deactivate SQS
    Lambda-->>API Gateway: 201 NHSDocumentReference
    deactivate Lambda
    API Gateway-->>React Web App: 201 NHSDocumentReference
    API Gateway-->>S3: PUT s3Url
    activate S3
    S3-->>API Gateway: 200
    deactivate S3
    deactivate API Gateway
    React Web App-->>GP Practice/PCSE User: Displays uploaded doc
    deactivate React Web App
        loop Every 5 mins
        Splunk-->>SQS: Polls for audit messages
    end
```
