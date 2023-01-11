# E2E Test

The E2E test runs against the entire system (i.e. UI, BE, and AWS services). The only test in this
suite [uploadAndDownload](cypress/e2e/uploadAndDownload.cy.js) describes the typical upload and download "happy-path"
journey.

## Prerequisites

-   [Node](https://nodejs.org/en/download/)
-   [npm](https://docs.npmjs.com/cli/v6/commands/npm-install)
-   [nvm](https://github.com/nvm-sh/nvm)

_Note: Node/npm versions can be managed using [nvm](https://github.com/nvm-sh/nvm). The version can be found in
the [.nvmrc](.nvmrc)._

## Running Locally

### 1. Install Dependencies

To install dependencies, run:

```bash
npm install
```

_Note: Ensure you have the correct Node version set before doing this._

### 2. Create Config

Create a config file by copying [cypress.env.json.example](cypress.env.json.example) and rename it
to `cypress.env.json.example`.

### 3. Setup Config

Add the following values to [cypress.env.json](cypress.env.json):

| Key                   | Value                    |
| --------------------- | ------------------------ |
| `username`            | `<AWS Cognito username>` |
| `password`            | `<AWS Cognito password>` |
| `REACT_APP_ENV`       | `local`                  |
| `basic_auth_username` | `%basic_auth_username%`  |
| `basic_auth_password` | `%basic_auth_username%`  |
| `oidc_provider`       | `cis2devoidc`            |

### 4. Run The Test

To start the E2E tests in open mode (with a visible browser window), run:

```bash
npm run test:open
```

For headless mode (without a visible browser window), run:

```bash
npm run test
```

_Note: E2E tests are run using [Cypress](https://www.cypress.io/) and require all services (incl. the UI) to be running
locally._

## CI

The E2E test stage is defined in [build.gocd.yaml](../gocd/build.gocd.yaml) and runs in both `dev` and `pre-prod`. This
stage utilises [tasks](../tasks) to run the installation, configuration, and the test itself.
