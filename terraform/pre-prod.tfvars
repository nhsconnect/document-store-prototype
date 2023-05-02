environment                  = "pre-prod"
account_id                   = "694282683086"
enable_session_auth                   = true
# cognito_domain_prefix        = "pre-prod-"
oidc_providers                        = ["cis2devoidc"]
cis2_provider_oidc_issuer             = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc"
cis2_provider_authorize_url           = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/authorize"
cis2_provider_token_url               = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/access_token"
cis2_provider_attributes_url          = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/userinfo"
cis2_provider_jwks_uri                = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/connect/jwk_uri"
cis2_client_callback_urls             = ["https://main.d3ltn2own5cx4o.amplifyapp.com/auth-callback"]
cis2_client_signout_urls              = ["https://main.d3ltn2own5cx4o.amplifyapp.com"]
cognito_key_id               = "hkmsNYnD3kvYvyaOWcuqPT2K5lad8tT8mWZU1sF6n14="
pds_fhir_is_stubbed          = "false"
cloud_storage_security_agent_role_arn = "arn:aws:iam::694282683086:role/CloudStorageSecAgentRole-d2ixsle"
quarantine_bucket_name                = "cloudstoragesecquarantine-694282683086-eu-west-2"