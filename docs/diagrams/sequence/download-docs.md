# Download Docs

The sequence diagram below illustrate the interactions that occur when a user downloads docs.

The diagram assumes that AWS Amplify has served the React web app; the user is logged in; has the required
permissions to download docs; and has found the patient they want to download docs for (where the sequence begins).

```mermaid
sequenceDiagram
    actor GP Practice/PCSE User
    GP Practice/PCSE User ->> React Web App: Lands on /search/results
    activate React Web App
    React Web App ->> GP Practice/PCSE User: Displays doc search progress
    React Web App ->> API Gateway: GET /DocumentReference
    activate API Gateway
    API Gateway ->> Lambda: Invokes DocumentReferenceSearchHandler
    activate Lambda
    Lambda ->> DynamoDB: query()
    Note over Lambda, DynamoDB: DocumentReferenceMetadata table
    activate DynamoDB
    DynamoDB ->> Lambda: documentMetadataPaginatedQueryList
    deactivate DynamoDB
    Lambda ->> API Gateway: 200 List<Document> as Bundle
    deactivate Lambda
    API Gateway ->> React Web App: 200 List<Document> as Bundle
    deactivate API Gateway
    React Web App ->> GP Practice/PCSE User: Displays docs found
    GP Practice/PCSE User ->> React Web App: Clicks download button
    React Web App ->> API Gateway: GET /DocumentManifest
    activate API Gateway
    API Gateway ->> Lambda: Invokes CreateDocumentManifestByNhsNumberHandler
    activate Lambda
    Lambda ->> DynamoDB: query()
    Note over Lambda, DynamoDB: DocumentReferenceMetadata table
    activate DynamoDB
    DynamoDB ->> Lambda: documentMetadataPaginatedQueryList
    deactivate DynamoDB
    loop Every document in document list
        Lambda -->> S3: getObject()
        activate S3
        S3 -->> Lambda: 200 S3Object
        deactivate S3
    end
    Lambda -->> S3: putObject()
    activate S3
    S3 -->> Lambda: PutObjectResult
    deactivate S3
    Lambda ->> DynamoDB: save()
    Note over Lambda, DynamoDB: DocumentZipTrace table
    Lambda -->> S3: generatePresignedUrl()
    activate S3
    S3 -->> Lambda: URL
    deactivate S3
    Lambda -->> SQS: sendMessage()
    Note over Lambda, SQS: <env>-sensitive-audit queue
    activate SQS
    SQS -->> Lambda: SendMessageResponse
    deactivate SQS
    Lambda ->> API Gateway: 200 preSignedUrl
    deactivate Lambda
    API Gateway ->> React Web App: 200 preSignedUrl
    deactivate API Gateway
    React Web App -->> S3: GET /<preSignedUrl>
    activate S3
    S3 -->> React Web App: 200 S3Object
    deactivate S3
    React Web App ->> GP Practice/PCSE User: Downloads ZIP file to machine
    deactivate React Web App
    loop Every 5 mins
        Splunk ->> SQS: Polls for audit messages
        Note over Splunk, SQS: <env>-sensitive-audit queue
    end
```
