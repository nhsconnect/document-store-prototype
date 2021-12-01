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

Variable name  | Description
-------------- | -----------
DS_TEST_HOST   | Overrides the host that Terraform and tests connect to instead of AWS (default: `localhost`).
EDGE_HOST_NAME | Overrides the host that LocalStack binds its edge service to (default: `127.0.0.1`).

To use this with Docker Machine, one might add the following to the Bash profile (or a utility
like [direnv](https://direnv.net/)):

```bash
export DS_TEST_HOST="$(docker-machine ip)"
export EDGE_HOST_NAME=0.0.0.0
```

## Running services locally

It is possible to run the prototype largely locally. This includes the backend Lambda functions and their associated
services (DynamoDB, S3, etc.), and the frontend UI.

### Starting the Document Store

The steps required to run the Document Store on a developer’s machine is largely covered in the previous
section on [testing](#testing), including information about setting up [environment variables](#environment-variables).
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

Placeholder      | Terraform output
-----------------|-----------------
`%region%`       | None. The value should be: `eu-west-2`
`%pool-id%`      | `cognito_user_pool_ids`
`%client-id%`    | `cognito_client_ids`
`%api-endpoint%` | `api_gateway_url`

Be careful not to commit these values along with other changes.

Once the `config.js` has been edited, the UI can be started from the `ui` subdirectory with `npm`:

```bash
npm run start
```
