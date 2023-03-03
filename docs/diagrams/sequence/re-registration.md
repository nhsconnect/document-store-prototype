# Re-Registration Event

The sequence diagram below illustrate the interactions that occur when a patient registers at a practice after having
been suspended.

The diagram assumes that a patient, with uploaded documents, has re-registered and a NEMS message was published
through the MESH mailbox. Then, the re-registration event has been sent to the ARF re-registration SQS queue (where the
sequence begins).

_Note: The MESH mailbox is owned by the ORC team._
_Note: This diagram does not include interactions with CloudWatch._

```mermaid
sequenceDiagram
    SPINE ->> SQS: Sends re-registration NEMS message
    Note over SPINE, SQS: <env>-re-registration queue
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
