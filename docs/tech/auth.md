# Auth

## Overview

Auth for the Access Request Fulfillment (ARF) service is achieved via either an AWS Cognito User Pool or the NHS CIS2
system. This
CIS2
system is integrated with AWS Cognito as an OIDC Identity Provider. There is a `cognito_oidc_providers` Terraform
variable that controls which auth method is active in each environment. In future iterations, the User
Pool will be removed, and the ARF service will solely use the CIS2 auth method. The auth has been
provisioned by Terraform.

## Cognito User Pools

### Auth Flow

1. User arrives at landing page.
2. User clicks on "Start now" button.
3. User is redirected to a protected page where we check if the user has an active Cognito session.
4. If they do, the user is able to access all protected pages in the ARF service. If they do not, the user is shown
   redirected to the OIDC login screen. Once they have signed in, they are able to access all protected pages in the
   ARF service.

### Config

In order to use Cognito User Pool auth, the `config.js` file must have the following settings:

- Auth providerId: "COGNITO"

### Creating A User

In order to log into the ARF service, you must first create a user in the Cognito User Pool. You can do this via the
AWS Console.

## Care Identity Service 2 (CIS2)

### Auth Flow

1. User arrives at landing page.
2. User clicks on "Start now" button.
3. User is redirected to a protected page where we check if the user has an active Cognito session.
4. If they do, the user is able to access all protected pages in the ARF service. If they do not, the user is
   redirected off-site to the CIS2 login page.
5. Once the user is successfully logged in to CIS2, they are redirected back to the configured
   url (`/auth-callback`)
   . If there are any errors present (e.g. Cognito misconfiguration), the user will be redirected back to the landing
   page with an error message. In the absence of any errors, the user will be redirected to the home page, and will be
   able to access all protected pages in the ARF service.

Further information about CIS2 can be
found [here](https://digital.nhs.uk/developer/guides-and-documentation/security-and-authorisation/user-restricted-restful-apis-nhs-cis2-combined-authentication-and-authorisation)
.

### Config

In order to use CIS2 auth, the `config.js` file must have the following settings:

- Auth providerId: "cis2devoidc"

### CIS2 Setup

In order to log into the ARF service using CIS2 auth, you must have dev accounts, which are provided
by the CIS2 team. This team also manages the OAuth redirect configuration. Please contact
nhscareidentityauthentication@nhs.net for further information. 