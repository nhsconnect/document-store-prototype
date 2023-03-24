environment                           = "dev"
account_id                            = "533825906475"
oidc_providers                        = ["cis2devoidc", "COGNITO"]
cis2_provider_oidc_issuer             = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc"
cis2_provider_authorize_url           = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/authorize"
cis2_provider_token_url               = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/access_token"
cis2_provider_attributes_url          = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/userinfo"
cis2_provider_jwks_uri                = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/connect/jwk_uri"
cis2_client_callback_urls             = ["http://localhost:3000/auth-callback"]
cis2_client_signout_urls              = ["http://localhost:3000"]
cognito_key_id                        = "owPwCSHr3lp0iS3IYF8OkOGFS/47dU6YOdDlVQztB5E="
pds_fhir_is_stubbed                   = "false"
cloud_storage_security_agent_role_arn = "arn:aws:iam::533825906475:role/CloudStorageSecAgentRole-3k85qxs"
quarantine_bucket_name                = "cloudstoragesecquarantine-3k85qxs-533825906475-eu-west-2"