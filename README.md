# Access Request Fulfilment (ARF) Service

Enables the NHS to fulfil their role as data controller and enable access requests. This is an interoperable service
that is capable of uploading, downloading, and deleting patient documents.

## Table Of Contents

1. [Prerequisites](#prerequisites)
2. [Running Locally](#running-locally)
3. [Testing](#testing)
4. [Running Services On AWS](#running-services-on-aws)
5. [Monitoring](#monitoring)
6. [Secrets](#secrets)
7. [Accessibility](#accessibility)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

- [Git](https://git-scm.com/)
- [Dojo](https://github.com/kudulab/dojo#installation)
- [Terraform](https://formulae.brew.sh/formula/terraform)
- [colima](https://formulae.brew.sh/formula/colima)
- [docker](https://formulae.brew.sh/formula/docker)
- [docker-compose](https://formulae.brew.sh/formula/docker-compose)
- [AWS CLI](https://aws.amazon.com/cli/)
- [nvm](https://formulae.brew.sh/formula/nvm)

_Note: It is recommended to use [Homebrew](https://brew.sh/) to install most of these._

## Running Locally

The ARF service can be run locally (excl. some AWS services and CIS2). The UI is run separately to the backend, see
the [UI README](ui/README.md) for more details. Auth will require configuration too.

[//]: # (TODO: Add details on how to configure auth)

### Running The Backend

#### 1. Start Colima (Or Another Docker Provider)

To start Colima, run:

```bash
colima start
```

#### 2. Set Env Variables

Create a `.env` file by duplicating [.env.example](.env.example) and adding any missing values. This file is sourced to
your shell env so make sure it doesn't have any extra whitespace, comments etc. Secrets can be obtained from AWS
Parameter Store.

#### 3. Start LocalStack

To start LocalStack run:

```bash
make start-localstack
```

After this, you should have a shell session open inside the default Dojo container. This has the AWS CLI, Terraform CLI,
and Gradle installed on it.

_Note: Do not close this window! That will stop LocalStack._

#### 4. Build The App & Deploy It To LocalStack

First, you will need to create a `terraform/local._override.tf` by
duplicating [terraform/local._override.tf.example](terraform/local._override.tf.example). Then, within the Dojo
container shell session, run:

```bash
make build-and-deploy-to-localstack
```

_Note: You do not need to create another `terraform/local._override.tf` after initial creation. However, you will need
to run the command above whenever any backend or Terraform changes are made._

### Logs

It may be useful to read the logs from LocalStack. This is done using the AWS CLI tool, pointing it at the LocalStack
container. The command looks like the following: `aws --endpoint-url=http://HOST:4566 logs tail /aws/lambda/HANDLER`,
where:

- `HOST` should be substituted for the hostname of the LocalStack Docker container
- `HANDLER` should be substituted for the name of the relevant controller

For instance, to read search logs with a native Docker service, one could run:

```bash
aws --endpoint-url=http://localhost:4566 logs tail /aws/lambda/DocumentReferenceSearchHandler
```

One may also follow log output as it happens by applying the `follow` flag to the `tail` subcommand:
`tail --follow HANDLER`.

## Testing

### Backend Tests

The backend (app) tests consist of unit and integration tests written in [JUnit](https://junit.org/junit5/). Unit and
integration tests can both be run using the following command:

```bash
make test-app
```

_Note: This command requires the backend to be running locally._

#### Unit Tests

Unit tests can be run using the following commands:

```bash
make test-app-unit
```

To run the unit tests with logs, run:

```bash
make test-app-unit-with-logs
```

#### Integration Tests

Integration tests will require the backend to be running locally and can be run using the following commands:

```bash
make test-app-integration
```

To run the integration tests with logs, run:

```bash
make test-app-integration-with-logs
```

### UI Tests

Details on the UI tests can be found in the [/ui README](ui/README.md).

### E2E Tests

Details on the E2E tests can be found in the [/e2eTest README](e2eTest/README.md).

[//]: # (TODO: Add details on auth tests)

## Running Services On AWS

### Auth

Ensure the correct role has been assumed before running any operations against
AWS, [see here for details](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-quickstart.html).

### Create Terraform State Bucket & The DynamoDB Locking Table

```bash
./bootstrap-terraform.sh "environment"
```

### Initialising GoCD Agents

In order to deploy to AWS from the pipeline, a GoCD agent must have a role and policy attached to it. These need to be
created before running the pipeline for the first time. This can be done by running the following Gradle tasks:

1. Create a CI Role:
    ```bash
    ./gradlew bootstrapCIRole
    ```
2. Attach a policy to a CI Role:
    ```bash
    ./gradlew attachPolicyToCIRole
    ```

### Basic Auth

By default, basic auth is enabled in all envs, to disable the basic auth. Set the Terraform variable `enable_basic_auth`
to `false`.

1. Basic auth username will be same the env name. e.g. `dev`, `pre-prod`
2. To get the password for the basic auth in the env:
    ```bash
   aws ssm get-parameter --name /prs/{ENVIRONMENT}/user-input/basic-auth-password --with-decryption
    ```

### Managing Cognito Users

There are commands available for creating and deleting Cognito users. You will need to assume a role with permission to
create and delete users in the relevant AWS account before running them.

#### Creating

```bash
./tasks create-cognito-user ${username} ${password}`
```

#### Deleting

```bash
./tasks delete-cognito-user ${username}
```

## Monitoring

We have configured AWS CloudWatch to provide alarm notifications whenever one of a number of metrics exceeds its normal
state. Currently, the only way to receive these notifications is by subscribing to an SNS topic using an email. You can
subscribe to the SNS topic once you have assumed an appropriate role using the AWS CLI. This is the command:

```bash
aws sns subscribe --topic-arn [topic-arn] --protocol email --notification-endpoint [your NHS email]
```

You will receive a confirmation email with a link allowing you to confirm the subscription. We are also subscribing to
the SNS topic using email addresses that are provided for Microsoft Teams channels.

## Secrets

To prevent secrets and sensitive info being pushed from a dev's machine, it is recommended to use a tool such
as [Talisman](https://thoughtworks.github.io/talisman/) that checks changes for anything that looks suspicious. It is
recommended
to [install this as a pre-commit hook](https://thoughtworks.github.io/talisman/docs/installation/global-hook/). There is
also a [.talismanrc](./.talismanrc) file that is used
to [configure Talisman, suppress flagged files, and prevent false positives](https://thoughtworks.github.io/talisman/docs/configuring-talisman).

## Accessibility

- [WAVE Chrome extension](https://chrome.google.com/webstore/detail/wave-evaluation-tool/jbbplnpkjmmeebjpijfedlgcdilocofh)
- Use a screen reader
- Use keyboard navigation
- Use NHS components rather than custom styling

## Troubleshooting

### Docker Daemon Is Not Running

If you see a log saying that the Docker daemon is not running when running `make start-localstack`, it is likely due
to Colima not being started. You can fix this by running `colima start`.

### nvm

If you're having problems downloading the Node version using [nvm](https://github.com/nvm-sh/nvm) then switch
to [n](https://formulae.brew.sh/formula/n) (or your favourite Node version
manager).

### LocalStack Timeout On Start

If you are experiencing timeouts when running `make start-localstack`, it is likely due to the Lima VM not having
enough resources allocated to it. You can add more resources to the Lima VM by running `colima start --edit` and
increasing the number of CPUs allocated to 4 and memory usage to 8GB.
