# Authentication

## Overview

Authentication for the Document Store is achieved via either an AWS Cognito User Pool or the NHS CIS2 system. This CIS2
system is integrated with AWS Cognito as an OIDC Identity Provider. There is a CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED
feature flag that controls which authentication method is active in each environment. In future iterations, the User
Pool will be removed, and the Document Store will solely use the CIS2 authentication method. The authentication has been
provisioned by Terraform.

### Cognito User Pools

#### Authentication Flow

1. User arrives on landing page
2. User clicks on "Start now" button
3. User is redirected to a protected page where we check if the user has an active Cognito session.
4. If they do, the user is able to access all protected pages in the Document Store. If they do not, the user is shown
   the Amplify log in component. Once they have signed in, they are able to access all protected pages in the Document
   Store.

#### Configuration

In order to use Cognito User Pool authentication, the config file must have the following settings:

- Auth providerId: "COGNITO"
- CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false

#### Creating a User

In order to log into the Document Store, you must first create a user in the Cognito User Pool. You can do this via the
AWS Console.

#### Amplify Components

The interface for logging in via Cognito User Pools is provided by AWS Amplify Components. We are yet to upgrade to the
latest version of the components. Documentation for the legacy components can be
found [here](https://github.com/aws-amplify/amplify-ui/tree/legacy/legacy/amplify-ui-react).

### Care Identity Service 2 (CIS2)

#### Authentication Flow

1. User arrives on landing page
2. User clicks on "Start now" button
3. User is redirected to a protected page where we check if the user has an active Cognito session.
4. If they do, the user is able to access all protected pages in the Document Store. If they do not, the user is
   redirected off-site to the CIS2 login page.
5. Once the user is successfully logged in to CIS2, they are redirected back to the configured url (/cis2-auth-callback)
   . If there are any errors present (e.g. Cognito misconfiguration), the user will be redirected back to the landing
   page with an error message. In the absence of any errors, the user will be redirected to the home page, and will be
   able to access all protected pages in the Document Store.

Further information about CIS2 can be
found [here](https://digital.nhs.uk/developer/guides-and-documentation/security-and-authorisation/user-restricted-restful-apis-nhs-cis2-combined-authentication-and-authorisation)
.

#### Configuration

In order to use CIS2 authentication, the config file must have the following settings:

- Auth providerId: "cis2devoidc"
- CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true

The following placeholder values must also be replaced:

| Placeholder      | Replacement values                   |
|------------------|----------------------------------------|
| `%cognito-domain%`| App integration domain name from AWS console |
| `%cognito-redirect-signin%`      | `%client-application-domain%/cis2-auth-callback`                |
| `%cognito-redirect-signout%`    | To be determined              |

#### CIS2 Setup

In order to log into the Document Store using CIS2 authentication, you must have developer accounts, which are provided
by the CIS2 team. This team also manages the OAuth redirect configuration. Please contact
nhscareidentityauthentication@nhs.net for further information. 