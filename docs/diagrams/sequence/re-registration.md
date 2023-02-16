# Re-Registration Event

The sequence diagram below illustrate the interactions that occur when a patient registers at a practice after having
been suspended.

The diagram assumes that the patient was successfully re-registered; a NEMS message was created for that re-registration
and made available to the Suspension Service through the MESH mailbox; Suspension Service successfully delivered the
re-registration event to ARF re-registration SQS queue (where the sequence begins); finally, some documents were
uploaded in the ARF service for that patient.

_Note: This diagram does not include interactions with CloudWatch._

```mermaid
sequenceDiagram
    Suspension Service ->> SQS: sendMessage()
    Note over Suspension Service, SQS: <env>-re-registration queue
    activate SQS
    SQS ->> Lambda: Invokes ReRegistrationEventHandler
    deactivate SQS
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
    Lambda ->> SQS: sendMessage()
    Note over Lambda, SQS: <env>-sensitive-nems-audit queue
    activate SQS
    SQS ->> Lambda: SendMessageResult
    deactivate SQS
    deactivate Lambda
    loop Every 5 mins
        Splunk -->> SQS: Polls for audit messages
        Note over Splunk, SQS: <env>-sensitive-nems-audit queue
    end
```
