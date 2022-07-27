# Document Store Prototype

Proof of concept implementation for an interoperable service capable of storing patient and clinical documents.

## Pre-requisites

- Java 11
- [Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli)
- [LocalStack](https://github.com/localstack/localstack)
- [Dojo](https://github.com/kudulab/dojo#installation)
- Git
- [AWS CLI](https://aws.amazon.com/cli/)
- [Docker](https://www.docker.com/products/docker-desktop)

For the UI, this also includes:

- Node v14.17.x
- [npm v6.14.x](https://docs.npmjs.com/cli/v6/configuring-npm/install)

## Testing

The `test` source set contains unit tests. These don't have any dependencies on infrastructure or external services.
These are run in CI. There is also a suite of E2E tests within the `e2eTest` source set which require LocalStack to
simulate AWS. Since we are using the open source version of LocalStack, we are unable to run the E2E tests in CI.

### Running E2E Tests

1. Start LocalStack:

```bash
./start-localstack
```

2. Configure LocalStack:

```bash
./gradlew bootstrapLocalStack   # or ./gradlew bLS 
```

3. Apply Terraform changes and start the E2E tests:

```bash
./gradlew e2eTest   # or ./gradlew eT
```

Steps 1 and 2 only need to be performed once before starting the E2E tests. Once LocalStack is running, the third step
can be done in isolation to apply any changes and re-run the tests.

### Running test harness
A subset of the end-to-end tests can be run as tests against any existing document store endpoint

Against AWS
```bash
 DOCUMENT_STORE_BASE_URI=<replace with api endpoint> API_AUTH=IAM ./gradlew testHarness:test
```

Against LocalStack
```bash
 DOCUMENT_STORE_BASE_URI=<replace with api endpoint> ./gradlew testHarness:test
```

When running against the AWS deployed API, run this before to set the AWS credentials needed to sign the API requests

```bash
eval $(assume-role doc-store)
```

When running against localstack the `DOCUMENT_STORE_BASE_URI` is `http://localhost:4566/restapis/<replace with rest api id>/<replace with stage name>/_user_request_/` where both the `rest api id` and `stage name` can be found in terraform output

### Reading logs

Useful logging output may not be revealed in the output from end-to-end tests. In that instance, it may be useful to
read the logs from LocalStack. This is done using the AWS CLI tool, pointing it at the LocalStack container. The command
looks like the following:

```bash
aws --endpoint-url=http://HOST:4566 logs tail /aws/lambda/HANDLER
```

where `HOST` should be substituted for the hostname of the LocalStack Docker container (see the
[Environment variables](#environment-variables) section for more information), and `HANDLER` should be substituted for
the name of the relevant controller. For instance, to read search logs with a native Docker service, one could run:

```bash
aws --endpoint-url=http://localhost:4566 logs tail /aws/lambda/DocumentReferenceSearchHandler
```

One may also follow log output as it happens by applying the `follow` flag to the `tail` subcommand:
`tail --follow HANDLER`.

### Environment variables

LocalStack and the E2E tests support a native Docker service running on `localhost`. Other setups, such as Docker
Machine, may need to target other IP addresses.

| Variable name  | Description                                                                                   |
|----------------|-----------------------------------------------------------------------------------------------|
| DS_TEST_HOST   | Overrides the host that Terraform and tests connect to instead of AWS (default: `localhost`). |
| EDGE_HOST_NAME | Overrides the host that LocalStack binds its edge service to (default: `127.0.0.1`).          |

To use this with Docker Machine, one might add the following to the Bash profile (or a utility
like [direnv](https://direnv.net/)):

```bash
export DS_TEST_HOST="$(docker-machine ip)"
export EDGE_HOST_NAME=0.0.0.0
```

## Running services locally

It is possible to run the prototype largely locally. This includes the backend Lambda functions and their associated
services (DynamoDB, S3, etc.), and the frontend UI.

To make the Localstack running on Local environment compatible with Apple Silicon use the env_variables

```bash
export LAMBDA_CONTAINER_REGISTRY=mlupin/docker-lambda
```

### Starting the Document Store

The steps required to run the Document Store on a developer’s machine is largely covered in the previous section on [testing](#testing), including information about setting up [environment variables](#environment-variables).

The relevant ones are repeated here for simplicity.

1. Start LocalStack:

```bash
./start-localstack
```

2. Configure LocalStack:

```bash
./gradlew bootstrapLocalStack   # or ./gradlew bLS
```

3. Deploy the application:

```bash
./gradlew deployToLocalStack   # or ./gradlew dTLS
```

The Terraform output from the deployment will include two important values:

- `api_gateway_rest_api_id`
- `api_gateway_rest_api_stage`

These can be used to construct requests with `curl` or Postman. The URLs will have the following form:

```
http://HOST:4566/restapis/API-ID/STAGE/_user_request_/PATH
```

where `HOST` is the hostname or IP of the Docker container for LocalStack, `API-ID` is the value from
`api_gateway_rest_api_id`, `STAGE` is the value from `api_gateway_rest_api_stage`, and `PATH` is the remainder of the
endpoint path. For example, to request the metadata for a document with ID `1234`, the URL might look like:

```
http://localhost:4566/restapis/ce33iruji1/test/_user_request_/DocumentReference/1234
```

### Starting the UI

As with any other React application built upon create-react-app, it can be served locally with hot reloading during
development. However, as the application is authenticated using Cognito, logging in with valid credentials is still a
necessity as is configuring the local application to connect to the relevant user pool.

During deployment, the `ui/src/config.js` is modified to include values necessary to connect to backend services. This
file needs to be modified to connect to a Cognito pool and the API Gateway. There are four placeholders to replace, with
names like `%region%`.

| Placeholder      | Terraform output                       |
|------------------|----------------------------------------|
| `%region%`       | None. The value should be: `eu-west-2` |
| `%pool-id%`      | `cognito_user_pool_ids`                |
| `%client-id%`    | `cognito_client_ids`                   |
| `%api-endpoint%` | `api_gateway_url`                      |

Be careful not to commit these values along with other changes.

Once the `config.js` has been edited, the UI can be started from the `ui` subdirectory with `npm`:

```bash
npm run start
```
## Running services on AWS

### AWS authentication

Before running any operations against AWS, ensure that you have [configured the command line interface](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html).

### Create terraform state bucket

#### Create bucket
```bash
aws s3api create-bucket --bucket document-store-terraform-state --acl private --create-bucket-configuration '{ "LocationConstraint": "eu-west-2" }'
```

#### Configure public access
```bash
aws s3api put-public-access-block --bucket document-store-terraform-state --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
```

#### Toggle on versioning
```bash
aws s3api put-bucket-versioning --bucket document-store-terraform-state --versioning-configuration Status=Enabled
```

### Initialising GoCD Agents
In order to deploy to AWS from the pipeline, a GoCD agent must have a role and policy attached to it. These need to be created before running the pipeline for the first time. This can be done by running the following gradle tasks:

1. Create a CI Role:
    ```bash
    ./gradlew bootstrapCIRole
    ```
2. Attach a policy to a CI Role:
    ```bash
    ./gradlew attachPolicyToCIRole
    ```

## APIs

### Create `DocumentReference`
Stores metadata to describe a new document. Includes a URL in the response for uploading the document.

#### Request

`POST /DocumentReference`

Headers:

| Name          | Value                                                                                   |
|---------------|-----------------------------------------------------------------------------------------|
| Accept        | application/fhir+json                                                                   |
| Authorization | [AWS signature](https://docs.aws.amazon.com/apigateway/api-reference/signing-requests/) |
| Content-Type  | application/fhir+json                                                                   |

Body:
```json
{
  "resourceType": "DocumentReference",
  "subject": {
    "identifier": {
      "system": "https://fhir.nhs.uk/Id/nhs-number",
      "value": "<number>"
    }
  },
  "type": {
    "coding": [
      {
        "system": "http://snomed.info/sct",
        "code": "<number>"
      }
    ]
  },
  "content": [
    {
      "attachment": {
        "contentType": "<mime-type>"
      }
    }
  ],
  "description": "<string>",
  "created": "<iso-8601-date-time>"
}
```

#### Response

Status: `201 Created`

Headers:

| Name         | Value                                                |
|--------------|------------------------------------------------------|
| Content-Type | application/fhir+json                                |
| Location     | Relative URL pointing to the new `DocumentReference` |

Body:
```json
{
  "resourceType": "DocumentReference",
  "id": "<string>",
  "subject": {
    "identifier": {
      "system": "https://fhir.nhs.uk/Id/nhs-number",
      "value": "<number>"
    }
  },
  "type": {
    "coding": [
      {
        "system": "http://snomed.info/sct",
        "code": "<number>"
      }
    ]
  },
  "content": [
    {
      "attachment": {
        "url": "<url>",
        "contentType": "<mime-type>"
      }
    }
  ],
  "docStatus": "preliminary",
  "description": "<string>",
  "created": "<iso-8601-date-time>"
}
```

#### Errors

##### Invalid coding system
Status: 400

Body:
```json
{
 "resourceType": "OperationOutcome",
 "issue": [
   {
     "severity": "error",
     "code": "code-invalid",
     "details": {
       "coding": [
         {
           "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
           "code": "INVALID_CODE_SYSTEM",
           "display": "Invalid code system"
         }
       ]
     }
   }
 ]
}
```

##### Internal server error
Status: 500

Body:
```json
{
 "resourceType": "OperationOutcome",
 "issue": [
   {
     "severity": "error",
     "code": "exception",
     "details": {
       "coding": [
         {
           "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
           "code": "INTERNAL_SERVER_ERROR",
           "display": "Internal server error"
         }
       ]
     }
   }
 ]
}
```

#### Examples

Request payload:
```json
{
 "resourceType": "DocumentReference",
 "subject": {
   "identifier": {
     "system": "https://fhir.nhs.uk/Id/nhs-number",
     "value": "9912312345"
   }
 },
 "type": {
   "coding": [
     {
       "system": "http://snomed.info/sct",
       "code": "185361000000102"
     }
   ]
 },
 "content": [
   {
     "attachment": {
       "contentType": "text/plain"
     }
   }
 ],
 "description": "uploaded document",
 "created": "2021-11-03T15:57:30Z"
}
```

Successful response payload:
```json
{
 "resourceType": "DocumentReference",
 "id": "a03566d7-b56f-499f-bd91-e53a1bf78f0d",
 "subject": {
   "identifier": {
     "system": "https://fhir.nhs.uk/Id/nhs-number",
     "value": "9713456776"
   }
 },
 "type": {
   "coding": [
     {
       "system": "http://snomed.info/sct",
       "code": "185361000000102"
     }
   ]
 },
 "content": [
   {
     "attachment": {
       "url": "<generated>",
       "contentType": "text/plain"
     }
   }
 ],
 "docStatus": "preliminary",
 "description": "uploaded document",
 "created": "2021-11-03T15:57:30Z"
}
```

### Retrieve `DocumentReference`
Returns the metadata for an existing document. Includes a URL in the response for downloading the document.

#### Request

`GET /DocumentReference/{id}`

Parameters:

| Name | Value                                     |
|------|-------------------------------------------|
| id   | The unique ID of the document to retrieve |

Headers:

| Name          | Value                                                                                   |
|---------------|-----------------------------------------------------------------------------------------|
| Accept        | application/fhir+json                                                                   |
| Authorization | [AWS signature](https://docs.aws.amazon.com/apigateway/api-reference/signing-requests/) |

#### Response

Status: `200 OK`

Headers:

| Name         | Value                                                |
|--------------|------------------------------------------------------|
| Content-Type | application/fhir+json                                |

Body:
```json
{
  "resourceType": "DocumentReference",
  "id": "<string>",
  "subject": {
    "identifier": {
      "system": "https://fhir.nhs.uk/Id/nhs-number",
      "value": "<number>"
    }
  },
  "type": {
    "coding": [
      {
        "system": "http://snomed.info/sct",
        "code": "<number>"
      }
    ]
  },
  "content": [
    {
      "attachment": {
        "url": "<url>",
        "contentType": "<mime-type>"
      }
    }
  ],
  "docStatus": "<preliminary | final>",
  "description": "<string>",
  "created": "<iso-8601-date-time>",
  "indexed": "<optional-iso-8601-date-time>"
}
```

The `docStatus` value in the response may be either `preliminary` or `final` depending on whether the document has been
uploaded. If the value is `preliminary`, the `indexed` field will be omitted. 

#### Errors

##### Document not found
Status: 404

Body:
```json
{
 "resourceType": "OperationOutcome",
 "issue": [
   {
     "severity": "error",
     "code": "not-found",
     "details": {
       "coding": [
         {
           "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
           "code": "NO_RECORD_FOUND",
           "display": "No record found"
         }
       ]
     }
   }
 ]
}
```

##### Internal server error
Status: 500

Body:
```json
{
 "resourceType": "OperationOutcome",
 "issue": [
   {
     "severity": "error",
     "code": "exception",
     "details": {
       "coding": [
         {
           "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
           "code": "INTERNAL_SERVER_ERROR",
           "display": "Internal server error"
         }
       ]
     }
   }
 ]
}
```

#### Examples

Successful response payload:
```json
{
 "resourceType": "DocumentReference",
 "id": "a03566d7-b56f-499f-bd91-e53a1bf78f0d",
 "subject": {
   "identifier": {
     "system": "https://fhir.nhs.uk/Id/nhs-number",
     "value": "9713456776"
   }
 },
 "type": {
   "coding": [
     {
       "system": "http://snomed.info/sct",
       "code": "185361000000102"
     }
   ]
 },
 "content": [
   {
     "attachment": {
       "url": "<generated>",
       "contentType": "text/plain"
     }
   }
 ],
 "docStatus": "final",
 "description": "uploaded document",
 "created": "2021-11-03T15:57:30Z",
 "indexed": "2021-11-03T15:58:14Z"
}
```

### Search by NHS number
Returns a `Bundle` of `DocumentReference`s with matching NHS numbers. The bundle will be empty if no matches are found.

`GET /DocumentReference?system.identifier=https://fhir.nhs.uk/Id/nhs-number|{nhs-number}`

Parameters:

| Name       | Value                    |
|------------|--------------------------|
| nhs-number | The patient’s NHS number |

Headers:

| Name          | Value                 |
|---------------|-----------------------|
| Accept        | application/fhir+json |
| Authorization | Bearer `token`        |

where `token` is an ID token issued by AWS Cognito.

#### Response

Status: `200 OK`

Headers:

| Name         | Value                                                |
|--------------|------------------------------------------------------|
| Content-Type | application/fhir+json                                |

Body:
```json
{
 "resourceType": "Bundle",
 "type": "searchset",
 "total": "<number>",
 "entry": [
   {
     "fullUrl": "<url-path>",
     "resource": {
       "resourceType": "DocumentReference",
       "id": "<string>",
       "subject": {
         "identifier": {
           "system": "https://fhir.nhs.uk/Id/nhs-number",
           "value": "<number>"
         }
       },
       "type": {
         "coding": [
           {
             "system": "http://snomed.info/sct",
             "code": "<number>"
           }
         ]
       },
       "docStatus": "<preliminary | final>",
       "content": [
         {
           "attachment": {
             "contentType": "<mime-type>",
             "url": "<generated>"
           }
         }
       ],
       "description": "<string>",
       "created": "<iso-8601-date-time>",
       "indexed": "<optional-iso-8601-date-time>"
     }
   }
 ]
}
```

where `entry` may consist of zero or more objects containing results.

#### Errors

##### Bad parameter
If no search parameter is provided, or it uses an invalid system identifier or value, a response will be returned with
the following structure. See the table below for the different values used in each error.

Status: 400

Body:
```json
{
 "resourceType": "OperationOutcome",
 "issue": [
   {
     "severity": "error",
     "code": "code-invalid",
     "details": {
       "coding": [
         {
           "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
           "code": "<error-code>",
           "display": "<error-message>"
         }
       ]
     }
   }
 ]
}
```

| Error type                     | Error code                | Error message             |
|--------------------------------|---------------------------|---------------------------|
| Unrecognised system identifier | INVALID_IDENTIFIER_SYSTEM | Invalid identifier system |
| Invalid subject identifier     | INVALID_IDENTIFIER_VALUE  | Invalid identifier value  |
| Missing search parameter       | INVALID_PARAMETER         | Invalid parameter         |        |


##### Internal server error
Status: 500

Body:
```json
{
 "resourceType": "OperationOutcome",
 "issue": [
   {
     "severity": "error",
     "code": "exception",
     "details": {
       "coding": [
         {
           "system": "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1",
           "code": "INTERNAL_SERVER_ERROR",
           "display": "Internal server error"
         }
       ]
     }
   }
 ]
}
```

#### Examples

No matches found:
```json
{
 "resourceType": "Bundle",
 "type": "searchset",
 "total": 0
}
```

Two matching documents:
```json
{
 "resourceType": "Bundle",
 "type": "searchset",
 "total": 2,
 "entry": [
   {
     "fullUrl": "/DocumentReference/a03566d7-b56f-499f-bd91-e53a1bf78f0d",
     "resource": {
       "resourceType": "DocumentReference",
       "id": "a03566d7-b56f-499f-bd91-e53a1bf78f0d",
       "subject": {
         "identifier": {
           "system": "https://fhir.nhs.uk/Id/nhs-number",
           "value": "9971234512"
         }
       },
       "type": {
         "coding": [
           {
             "system": "http://snomed.info/sct",
             "code": "185361000000102"
           }
         ]
       },
       "docStatus": "final",
       "content": [
         {
           "attachment": {
             "contentType": "text/plain",
             "url": "<generated>"
           }
         }
       ],
       "description": "uploaded document",
       "created": "2021-11-04T15:57:30Z",
       "indexed": "2021-11-04T15:58:14Z"
     }
   },
   {
     "fullUrl": "/DocumentReference/e53a16d7-b56f-499f-bd91-e53a1bf78f0d",
     "resource": {
       "resourceType": "DocumentReference",
       "id": "e53a16d7-b56f-499f-bd91-e53a1bf78f0d",
       "subject": {
         "identifier": {
           "system": "https://fhir.nhs.uk/Id/nhs-number",
           "value": "9174512345"
         }
       },
       "type": {
         "coding": [
           {
             "system": "http://snomed.info/sct",
             "code": "185361000000102"
           }
         ]
       },
       "docStatus": "preliminary"
     }
   }
 ]
}
```
