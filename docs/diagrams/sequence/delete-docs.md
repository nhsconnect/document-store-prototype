# Delete Docs

The sequence diagram below illustrate the interactions that occur when a user deletes all docs.

The diagram assumes that AWS Amplify has served the React web app; the user is logged in; has the required
permissions to delete docs; and has found the patient, with docs uploaded, that they want to delete docs for (where the
sequence begins).

_Note: This diagram does not include interactions with CloudWatch._

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
    GP Practice/PCSE User ->> React Web App: Clicks delete button
    React Web App ->> GP Practice/PCSE User: Navigates to /search/results/delete
    GP Practice/PCSE User ->> React Web App: Clicks delete confirmation button
    React Web App ->> GP Practice/PCSE User: Displays deletion progress
    React Web App ->> API Gateway: DELETE /DocumentReference
    activate API Gateway
    API Gateway ->> Lambda: Invokes DeleteDocumentReferenceHandler
    activate Lambda
    Lambda ->> DynamoDB: query()
    Note over Lambda, DynamoDB: DocumentReferenceMetadata table
    activate DynamoDB
    DynamoDB ->> Lambda: documentMetadataPaginatedQueryList
    deactivate DynamoDB
    loop Every document in document list
        Lambda ->> DynamoDB: save()
        Note over Lambda, DynamoDB: DocumentReferenceMetadata table
    end
    loop Every document in document list
        Lambda -->> S3: deleteObject()
    end
    Lambda -->> SQS: sendMessage()
    Note over Lambda, SQS: <env>-sensitive-audit queue
    activate SQS
    SQS -->> Lambda: SendMessageResult
    deactivate SQS
    Lambda ->> API Gateway: 200 Successfully deleted message
    deactivate Lambda
    API Gateway ->> React Web App: 200 Successfully deleted message
    deactivate API Gateway
    React Web App ->> GP Practice/PCSE User: Navigates to /search/results
    deactivate React Web App
    loop Every 5 mins
        Splunk ->> SQS: Polls for audit messages
        Note over Splunk, SQS: <env>-sensitive-audit queue
    end
```
