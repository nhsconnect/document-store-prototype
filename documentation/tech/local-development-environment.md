# The Local Development Environment

This document describes how the local development environment actually functions, as opposed to how to operate it (see
README.md for that).

## Possible Structures

- Entrypoint in (i.e. start from "what happens when I run `./tasks start-localstack`" and go from there)
- By technology (Localstack/Dojo/Docker-compose/Terraform etc.)
- By challenge:
    - Works on my machine
    - Replicate AWS env
    - Missing LocalStack features
    - Building and deploying lambdas and web app

## What happens when I run `./tasks start-localstack`?

The `tasks` file is a bash script containing most of the command required to provision and manage the application,
including starting the app, building the project, and provisioning AWS resources. It is used in both local and cloud
situations.

The `start-localstack` task invokes a program called [Dojo](https://github.com/kudulab/dojo), which is a wrapper around
Docker built by a Thoughtworker. Dojo provides several benefits including ensuring containers handle termination signals
properly, configuring Docker users correctly and forwarding environment variables into the containers.

The dojo command does a couple of things. First, it generates a `docker-compose.yaml.dojo` file which specifies a
default container based on the image specified in `Dojofile-integration`, and environment variables and volumes for the
other containers specified in the main `docker-compose.yml`. Secondly, it runs `docker-compose` with the `-f` flag to
merge the config from `docker-compose.yml` and `docker-compose.yaml.dojo`. Neither of the YAML files is a valid docker
compose file in its own right, only when merged together in a single docker compose command can the project run.

## What does docker compose?

The first container docker compose starts is the [LocalStack](https://localstack.cloud/) container. This takes a few
seconds to become healthy, so we have the next container `bootstrap-terraform` wait for a "healthy" signal from
LocalStack, as it can't run until LocalStack has made instances of AWS S3 and DynamoDB available.
The `bootstrap-terraform` container is an AWS CLI container which runs our`bootstrap-terraform.sh` script. This script
is responsible for creating an S3 bucket and a
DynamoDB table to store the terraform state for our local development environment in. These need to be re-created every
time we start the project, because LocalStack's community edition does not persist its state in a volume.

Once `bootstrap-terraform` has completed successfully, we start the `terraform` container. This container runs
the `deploy-to-localstack.sh` script, which plans and applies the terraform using the local terraform backend created
by `bootstrap-terraform.sh` and the local terraform variables in `local.tfvars`. Again, the AWS resources provisioned
are not maintained once the LocalStack container exits (i.e. once the docker compose command stops running).

Finally, once terraform has finished, we start the `default` container, which is actually just an OpenJDK container with
a few modifications to allow it to work with
Dojo. [This is the image repository](https://github.com/nhsconnect/prm-deductions-docker-openjdk-dojo). Dojo
automatically creates a terminal session inside the default container so that you can do stuff like compile the java
application or run unit tests from inside the docker network, which is useful for ensuring that, for example, everyone
is using the right OpenJDK version. Exiting the terminal session brings down the local environment and wipes the
LocalStack state.






