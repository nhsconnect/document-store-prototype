environment                          = "dev"
account_id                           = "533825906475"
cognito_oidc_providers               = ["cis2devoidc", "COGNITO"]
cognito_cis2_provider_oidc_issuer    = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/openam/oauth2/realms/root/realms/NHSIdentity/realms/Healthcare/oidc"
cognito_cis2_provider_authorize_url  = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/openam/oauth2/realms/root/realms/NHSIdentity/realms/Healthcare/authorize"
cognito_cis2_provider_token_url      = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/openam/oauth2/realms/root/realms/NHSIdentity/realms/Healthcare/access_token"
cognito_cis2_provider_attributes_url = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/openam/oauth2/realms/root/realms/NHSIdentity/realms/Healthcare/userinfo"
cognito_cis2_provider_jwks_uri       = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/NHSIdentity/realms/Healthcare/connect/jwk_uri"
cognito_cis2_client_callback_urls    = ["http://localhost:3000/cis2-auth-callback"]
cognito_cis2_client_signout_urls     = ["http://localhost:3000"]
cognito_key_id                       = "owPwCSHr3lp0iS3IYF8OkOGFS/47dU6YOdDlVQztB5E="
pds_fhir_is_stubbed                  = "false"