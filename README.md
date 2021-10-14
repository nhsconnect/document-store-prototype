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

The test source set contains unit tests. These don't have any dependencies on infrastructure or external services. These are run in CI.
There is also a suite of E2e tests within the e2eTest source set which require localstack to simulate AWS. Since we are using the opensource version on localstack, we are unable to run the E2eTests on CI.

### Running E2e Tests

Steps:
1. Run localstack: 
```
SERVICES=apigateway,lambda,s3,iam,logs,dynamodb localstack start
```
2. From the terraform subdirectory, run these commands:
```
terraform init
terraform apply -var-file=local.tfvars
```

3. From the project root directory, run the E2e tests:
```
./gradlew e2eTest
```
