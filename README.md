# Document Store Prototype

Proof of concept implementation for an interoperable service capable of storing patient and clinical documents.

## Prerequisites

- [Git](https://git-scm.com/)
- [Dojo](https://github.com/kudulab/dojo#installation)
- [Terraform](https://formulae.brew.sh/formula/terraform)
- [colima](https://formulae.brew.sh/formula/colima)
- [docker](https://formulae.brew.sh/formula/docker)
- [docker-compose](https://formulae.brew.sh/formula/docker-compose)
- [git-mob](https://www.npmjs.com/package/git-mob)
- [AWS CLI](https://aws.amazon.com/cli/)
- [nvm](https://formulae.brew.sh/formula/nvm)

_Note: It is recommended to use [Homebrew](https://brew.sh/) to install most of these._
Please refer to the troubleshooting section for known problems

## Running Locally

It is possible to run the Document Store backend locally (excl. Cognito and CIS2). Auth through the UI will
still require either Cognito to be set up in AWS, or CIS2 to be configured.

### Running The Document Store

#### 1. Start colima (or any Docker provider)

To start colima, run:

```bash
colima start
```

#### 2. Set Environment variables

Create a .env file by copying the .env.example file and adding any missing values. This file is sourced to your shell environment so make sure it doesn't have
any extra whitespace, comments etc. Secrets can be obtained from AWS Parameter Store.

#### 3. Start LocalStack

To start LocalStack run:

```bash
./tasks start-localstack
```

After this you should have a shell session open inside the default Dojo container, which has the AWS CLI, terraform CLI, and gradle installed on it.

**Warning: Do not close this window! That will stop LocalStack.**

#### 4 Build the app and deploy it to LocalStack.

First, you will need to copy `terraform/local._override.tf.example` to `terraform/local._override.tf`. Then:

```bash
./tasks _deploy-to-localstack
```

### Running The UI

For info on the UI, visit the [UI README](ui/README.md).

## Running Services On AWS

### AWS Auth

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
to false.

1. Basic auth username will be same the env name. e.g. `dev`, `pre-prod`
2. To get the password for the basic auth in the env:
    ```bash
       aws ssm get-parameter --name /prs/{ENVIRONMENT}/user-input/basic-auth-password --with-decryption
    ```

### Managing Cognito Users

There are commands available for creating and deleting Cognito users. You will need to assume a role with permission to
create and delete users in the relevant AWS account before running them.

Creating:

```bash
  ./tasks create-cognito-user ${username} ${password}`
```

Deleting:

```bash
  ./tasks delete-cognito-user ${username}
```

## Testing

The `test` source set contains unit tests. These don't have any dependencies on infra or external services.
There is also a suite of API tests within the `integrationTest` source set which run against AWS resources.

### E2E Test

The E2E test runs against the entire system (i.e. UI, BE, and AWS services). These can be found in [/e2eTest](e2eTest)
and more details can be found in the [README](e2eTest/README.md).

### Reading Logs

Useful logging output may not be revealed in the output from E2E tests. In that instance, it may be useful to
read the logs from LocalStack. This is done using the AWS CLI tool, pointing it at the LocalStack container. The command
looks like the following: `aws --endpoint-url=http://HOST:4566 logs tail /aws/lambda/HANDLER`, where:

- `HOST` should be substituted for the hostname of the LocalStack Docker container (see the
  [env variables](#env-variables) section for more info)
- `HANDLER` should be substituted for the name of the relevant controller

For instance, to read search logs with a native Docker service, one could run:

```bash
aws --endpoint-url=http://localhost:4566 logs tail /aws/lambda/DocumentReferenceSearchHandler
```

One may also follow log output as it happens by applying the `follow` flag to the `tail` subcommand:
`tail --follow HANDLER`.

## Monitoring

We have configured AWS CloudWatch to provide alarm notifications whenever one of a number of metrics exceeds its normal state.
Currently, the only way to receive these notifications is by subscribing to an SNS topic using an email. You can subscribe
to the SNS topic once you have assumed an appropriate role using the AWS CLI. This is the command:

```bash
aws sns subscribe --topic-arn [topic-arn] --protocol email --notification-endpoint [your NHS email]
```

You will receive a confirmation email with a link allowing you to confirm the subscription.

We are also subscribing to the SNS topic using email addresses that are provided for Microsoft Teams channels.

## Troubleshooting

### Docker Daemon Is Not Running

If you see a log saying that the docker daemon is not running when running `./tasks start-localstack`, it is likely due
to colima not being started. You can fix this by running `colima start`.

### nvm

If you're having problems downloading the node version using nvm then switch to n (or your favourite node package
manager)
[n](https://formulae.brew.sh/formula/n)

### LocalStack Timeout On Start

If you are experiencing timeouts when running `./tasks start-localstack`, it is likely due to the Lima VM not having
enough resources allocated to it. You can add more resources to the Lima VM by running `colima start --edit` and
increasing the number of CPUs allocated to 4 and memory usage to 8GB.

## Accessibility Requirements

Make sure to check these during QA:

- Use [WAVE Chrome extension](https://chrome.google.com/webstore/detail/wave-evaluation-tool/jbbplnpkjmmeebjpijfedlgcdilocofh)
- Need to use a screen reader 
- Check keyboard navigation 
- Try to use NHS components rather than our own styling
