# PDS FHIR API Integration

Users are required to perform a "verify NHS number" operation against the Personal Demographics Service (PDS) before they
upload/download documents within Access Request Fulfilment Service (ARF). Therefore, the ARF service is integrated with the PDS FHIR API.

Our users input an NHS number from their document(s), and we use that NHS number to fetch results from the PDS FHIR API.
The PDS FHIR API returns a list of patients matching with the NHS number (the list should only have a single patient but
this is not guaranteed). We then display some of the patient's data to the user so that they can verify the patient is whom
they were expecting.

## Authentication with the API

ARF uses the application-restricted authentication method (see [Application Restricted API Authentication](https://digital.nhs.uk/developer/guides-and-documentation/security-and-authorisation/application-restricted-restful-apis-signed-jwt-authentication)).
This requires us to generate a signed JWT to include with each request to the PDS FHIR API and provide the PDS service with
a public key it can use to verify the signature. Key pairs can be generated and persisted to AWS parameter store for each 
environment using `./create-key-pair.sh ${ENVIRONMENT}`, e.g. `./create-key-pair.sh dev`.

Once a key pair has been generated, the public Json Web Key Set (JWKS) must be provided to PDS. Once we go live, in the production environment
this is done by placing the JWKS inside the public directory of the React project and deploying it to AWS Amplify (see the `configure-ui` task).

However, at present all environments are behind basic auth, and therefore PDS is unable to fetch the JWKS using a URL to Amplify.
Fortunately, PDS also allows you to upload the JWKS via the API registration portal. This is fine before we go live, but once
live we should use the URL approach so that we can rotate the key frequently without having to remember to upload the new JWKS
to the API portal each time.

To provide the public JWKS to PDS you will need to create an account using your main NHS email on the [NHS API portal](https://onboarding.prod.api.platform.nhs.uk/).
Before you can manage the application, you must be added to the Access Request Fulfilment Service team within the portal by a team
owner. The current owners are:

- Simon Brunning
- Richard Whitehead
- Ryan Brown

## Extracting the relevant patient verification factors from the response

One of the most complex aspects of the "verify NHS number" operation is that it relies on the patient's postal code and
name data. However, as noted in the [documentation](https://digital.nhs.uk/developer/api-catalogue/personal-demographics-service-fhir),
patient's may have multiple names and/or addresses on their record. We have had it confirmed by the PDS team that we can
safely assume a patient will have a "current home address" and a "current usual name". We have created a set of DTOs to make
it easy to access the current home address and current usual name for a patient object. These are located at `app/src/main/java/uk/nhs/digital/docstore/patientdetails/fhirdtos`.

## Stubbing the API response

We provide a fake for the PDS FHIR API service for testing purposes, which returns a static set of patient data for any
valid NHS number. The fake implementation can be toggled on or off in an environment by manipulating the "pds_fhir_is_stubbed" terraform
variable and re-deploying terraform.

## Resources

- [API Registration Portal](https://onboarding.prod.api.platform.nhs.uk/)
- [Application Restricted API Authentication](https://digital.nhs.uk/developer/guides-and-documentation/security-and-authorisation/application-restricted-restful-apis-signed-jwt-authentication)
- [PDS FHIR API Documentation](https://digital.nhs.uk/developer/api-catalogue/personal-demographics-service-fhir)
