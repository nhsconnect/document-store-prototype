# UI

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

Create a config file by copying [config.js.local.example](src/config.js.local.example) and rename it to `config.js`.

_Note: If cognito user pool is modified, `userPoolId` and `userPoolWebClientId` will need to be changed in the config
file and replaced by the new values that can be found in Cognito User Pool in the AWS Console._

### 3. Setup Config

The [config.js](src/config.js) file must be modified to include values necessary to connect to backend services. This
file needs to be modified to connect to a Cognito pool and the API Gateway. There are two placeholders to replace in the
API gateway configuration.

| Placeholder           | Terraform output    |
| --------------------- | ------------------- |
| `%api-gateway-id%`    | `api_gateway_id`    |
| `%api-gateway-stage%` | `api_gateway_stage` |

You can view the Terraform output by running:

```bash
./tasks view-terraform-logs
```

### 4. Start The App

The UI can be started by running:

```bash
npm start
```

_Note: Do not exit the program/terminal whilst running the UI or Cypress tests. The can run the latter in another
terminal window._

### 5. Log Into The App

The app is authenticated using Cognito, logging in with valid credentials is still a
necessity as is configuring the local app to connect to the relevant user pool.

## Testing

### Unit Tests

Unit tests are run using [Jest](https://jestjs.io/)
and [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/). They are ran using the
following command:

```bash
npm test
```

## Feature Toggles

We have implemented a rudimentary feature toggle system using the `config.js` file. Feature activation is determined at
build time depending on the `NODE_ENV` environment variable. There is a feature toggle
React [context provider](src/providers/FeatureToggleProvider.jsx) and custom hook for checking the value of the
toggle.

## Design

The UI follows the guidelines specified in the [NHS Service Manual](https://service-manual.nhs.uk/). To help achieve
this, we utilise the UI components provided by the [nhsuk-frontend](https://github.com/nhsuk/nhsuk-frontend) package.
