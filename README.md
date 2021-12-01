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
