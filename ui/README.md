# UI

## Table Of Contents

1. [Prerequisites](#prerequisites)
2. [Running Locally](#running-locally)
3. [Testing](#testing)
4. [Feature Toggles](#feature-toggles)
5. [Design](#design)

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

Or from the root directory, run:

```bash
make install-ui
```

_Note: Ensure you have the correct Node version set before doing this (i.e. `nvm use`)._

### 2. Create Config

Create a config file by copying [config.js.local.example](src/config.js.local.example) and rename it to `config.js`.

### 3. Setup Config

The [config.js](src/config.js) file must be modified to include values necessary to connect to backend services. This
file needs to be modified to connect to the API Gateway. There are two placeholders to replace in the
API gateway config. The placeholder values can be found in the Terraform output.

| Placeholder           | Terraform output    |
| --------------------- | ------------------- |
| `%api-gateway-id%`    | `api_gateway_id`    |
| `%api-gateway-stage%` | `api_gateway_stage` |

### 4. Start The UI

To start the UI, run:

```bash
npm start
```

Or from the root directory, run:

```bash
make start-ui
```

### 5. Log Into The

The app is authenticated
using [CIS2](https://digital.nhs.uk/services/identity-and-access-management/nhs-care-identity-service-2), logging in
with valid credentials is still a necessity as is configuring the local app to connect to the relevant user pool.

## Testing

### Unit Tests

Unit tests are run using [Jest](https://jestjs.io/)
and [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/). They are ran using the
following command:

```bash
npm test
```

Or from the root directory, run:

```bash
make test-ui
```

## Feature Toggles

We have implemented a rudimentary feature toggle system using [config.js](src/config.js). Feature activation is
determined at
build time depending on the `NODE_ENV` env variable. There is
a [React context config provider](src/providers/configProvider/ConfigProvider.jsx) and a custom hook for checking the
value of
the toggle.

## Design

The UI follows the guidelines specified in the [NHS Service Manual](https://service-manual.nhs.uk/). To help achieve
this, we utilise the UI components provided by the [nhsuk-frontend](https://github.com/nhsuk/nhsuk-frontend) package.
