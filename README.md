# Document Store Prototype

Proof of concept implementation for an interoperable service capable of storing patient and clinical documents.

## Prerequisites

- [Git](https://git-scm.com/)
- [Dojo](https://github.com/kudulab/dojo#installation)
- [colima](https://formulae.brew.sh/formula/colima)
- [docker](https://formulae.brew.sh/formula/docker)
- [docker-compose](https://formulae.brew.sh/formula/docker-compose)
- [git-mob](https://www.npmjs.com/package/git-mob)
- [Node](https://nodejs.org/en/download/): `v14.17.x`
- [npm](https://docs.npmjs.com/cli/v6/commands/npm-install): `v6.14.x`
- [AWS CLI](https://aws.amazon.com/cli/)

_Note: It is recommended to use [Homebrew](https://brew.sh/) to install most of these._

## Running Locally

It is possible to run the Document Store backend locally (minus Cognito or CIS2). Authentication through the UI will
still require either Cognito to be set up in AWS, or CIS2 to be configured (depending on the value of
the `CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED` feature toggle on the frontend).

### Running The Document Store

#### 1. Start LocalStack

To start LocalStack (incl. bootstrap and applying to Terraform), run:

```bash
./tasks start-localstack
```

**Warning: Do not close this window! You will need this when running other services.**

_Note: This will deploy your API Lambdas if their JARs are already built._

#### 2. Build API Lambda JARs

To build/re-build these into your `app/build/libs` dir, run in a non-dojo terminal window:

```bash
./tasks build-api-jars
```

3. Deploy or re-deploy the API:

```bash
./tasks deploy-to-localstack
```

The Terraform output from the deployment will include two important values:

- `api_gateway_rest_api_id`
- `api_gateway_rest_api_stage`

These can be used to construct requests with `curl` or Postman, and also to construct the API endpoint
in `ui/src/config.js`. The URLs will have the following form:

```
http://HOST:3000/restapis/API-ID/STAGE/_user_request_/PATH
```

where

- `HOST` is the hostname or IP of the Docker container for LocalStack. Within the 'docker-compose' containers
  this is `localstack`, from outside in your host laptop this is `localhost` because localstack is exposed on the local
  4566 port.
- `API-ID` is the value from
- `api_gateway_rest_api_id`, `STAGE` is the value from `api_gateway_rest_api_stage`, and `PATH` is the remainder of the
  endpoint path. For example, to request the metadata for a document with ID `1234`, the URL might look like:

```
http://localhost:3000/restapis/ce33iruji1/test/_user_request_/DocumentReference/1234
```

(TODO - set this up as an ENV variable automatically?)

### Starting the UI

For information on starting and testing the UI, please visit [the UI ReadMe](/ui/README.md)

## Running services on AWS

### AWS authentication

Ensure the correct role has been assumed before running any operations against
AWS, [see here for details](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html).

### Create terraform state bucket and the DynamoDB locking table

  ```bash
    ./bootstrap-terraform.sh "environment"
  ```

example: `./bootstrap-terraform.sh pre-prod`

### Initialising GoCD Agents

In order to deploy to AWS from the pipeline, a GoCD agent must have a role and policy attached to it. These need to be
created before running the pipeline for the first time. This can be done by running the following gradle tasks:

1. Create a CI Role:
    ```bash
    ./gradlew bootstrapCIRole
    ```
2. Attach a policy to a CI Role:
    ```bash
    ./gradlew attachPolicyToCIRole
    ```

### Basic Auth

By default, basic auth is enabled in all environments, to disable the basic auth `enable_basic_auth` terraform variable
can be set to false

1. Basic auth username will be same the environment name. eg dev, pre-prod
2. To get the password for the basic auth in the environment

```bash
   aws ssm get-parameter --name /prs/{ENVIRONMENT}/user-input/basic-auth-password --with-decryption
```

## Testing

The `test` source set contains unit tests. These don't have any dependencies on infrastructure or external services.
These are run in CI. There is also a suite of API tests within the `e2eTest` source set which require LocalStack to
simulate AWS. Since we are using the open source version of LocalStack, we are unable to run the API tests in CI.

There also a set of E2E test that run in the browser using Cypress. See [the UI ReadMe](/ui/README.md)

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
| EDGE_HOST_NAME | Overrides the host that LocalStack binds its edge service to (default: `127.0.0.1`).          |

To use this with Docker Machine, one might add the following to the Bash profile (or a utility
like [direnv](https://direnv.net/)):

```bash
export EDGE_HOST_NAME=0.0.0.0
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

### Retrieve `Patient` details

Returns a subset of patient details for patient trace purposes

#### Request

`GET /PatientDetails?system.identifier=https://fhir.nhs.uk/Id/nhs-number|{nhs-number}`

Parameters:

| Name       | Value                    |
|------------|--------------------------|
| nhs-number | The patient's NHS number |

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
  "resourceType": "Bundle",
  "type": "searchset",
  "total": 1,
  "entry": [
    {
      "resource": {
        "resourceType": "Patient",
        "identifier": [
          {
            "extension": [
              {
                "url": "https://fhir.hl7.org.uk/StructureDefinition/Extension-UKCore-NHSNumberVerificationStatus",
                "valueCodeableConcept": {
                  "coding": [
                    {
                      "system": "https://fhir.hl7.org.uk/CodeSystem/UKCore-NHSNumberVerificationStatus",
                      "version": "1.0.0",
                      "code": "01",
                      "display": "Number present and verified"
                    }
                  ]
                }
              }
            ],
            "system": "https://fhir.nhs.uk/Id/nhs-number",
            "value": "9000000009"
          }
        ],
        "id": "9000000009",
        "name": [
          {
            "use": "usual",
            "given": [
              "Jane"
            ],
            "family": "Doe"
          }
        ],
        "birthDate": "1998-07-11",
        "address": [
          {
            "use": "home",
            "postalCode": "LS1 6AE",
            "extension": [
              {
                "url": "https://fhir.hl7.org.uk/StructureDefinition/Extension-UKCore-AddressKey",
                "extension": [
                  {
                    "url": "type",
                    "valueCoding": {
                      "system": "https://fhir.hl7.org.uk/CodeSystem/UKCore-AddressKeyType",
                      "code": "PAF"
                    }
                  },
                  {
                    "url": "value",
                    "valueString": "12345678"
                  }
                ]
              }
            ]
          }
        ]
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

## Troubleshooting

### Docker Daemon Is Not Running

If you see a log saying that the docker daemon is not running when running `./tasks start-localstack`, it is likely due
to colima not being started. You can fix this by running `colima start`.

### LocalStack Timeout On Start

If you are experiencing timeouts when running `./tasks start-localstack`, it is likely due to the Lima VM not having
enough resources allocated to it. You can add more resources to the Lima VM by running `colima start --edit` and
increasing the number of CPUs allocated and memory usage. 
