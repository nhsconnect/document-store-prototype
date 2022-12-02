# UI

## Pre-requisites

Before starting the UI, install dependencies using:

```bash
npm ci
```

## Starting the UI

As with any other React application built upon create-react-app, it can be served locally with hot reloading during
development. However, as the application is authenticated using Cognito, logging in with valid credentials is still a
necessity as is configuring the local application to connect to the relevant user pool.

During deployment, the `ui/src/config.js` is modified to include values necessary to connect to backend services. This
file needs to be modified to connect to a Cognito pool and the API Gateway. There are four placeholders to replace, with
names like `%region%`. See `ui/src/config.js.local.example` for guidance / template for running vs localstack.

| Placeholder      | Terraform output                                                                                                                                     |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `%region%`       | None. The value should be: `eu-west-2`                                                                                                               |
| `%pool-id%`      | `cognito_user_pool_ids`                                                                                                                              |
| `%client-id%`    | `cognito_client_ids`                                                                                                                                 |
| `%api-endpoint%` | `api_gateway_url` or something like `http://localhost:3000/restapis/3sqfccx8m1/test/_user_request_` with your specific API gateway id vs localstack. |

Be careful not to commit these values along with other changes.

Once the `config.js` has been edited, the UI can be started from the `ui` subdirectory with `npm`:

```bash
npm run start
```

Don't close this while looking at the UI or running the cypress tests - you'll need to run the rest of this in a new
terminal.

## Testing the UI

### Unit Tests

Unit tests are run using Jest test runner. To run all units tests:

```bash
npm test
```

### E2E Tests

E2E tests are run using Cypress. Before running the tests, you must set valid AWS Cognito credentials. Copy
the `cypress.env.json.example` file and rename the file to `cypress.env.json`. Replace the empty strings with a valid
AWS Cognito username and password. Ensure the `config.js` file has been configured as explained above, and that
localstack is running.

To start Cypress:

```bash
npx cypress open
```

## Feature Toggles

We have implemented a rudimentary feature toggle system using the `config.js` file. Feature activation is determined at
build time depending on the `NODE_ENV` environment variable. There is a feature toggle
React [context provider](ui/src/providers/FeatureToggleProvider.jsx) and custom hook for checking the value of the
toggle.
