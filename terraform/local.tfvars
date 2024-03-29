environment                  = "local"
account_id                   = ""
api_gateway_stage            = "test"
disable_aws_remote_checks    = true
cloud_only_service_instances = 0
aws_endpoint                 = "http://localstack:4566"
dynamodb_endpoint            = "http://localstack:4566"
s3_endpoint                  = "http://localstack:4566"
s3_use_path_style            = true
pds_fhir_is_stubbed          = "true"
enable_basic_auth            = false
sqs_endpoint                 = "http://localhost:4566"
cis2_provider_oidc_issuer    = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc"
cis2_provider_authorize_url  = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/authorize"
cis2_provider_token_url      = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/access_token"
cis2_provider_user_info_url  = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/userinfo"
cis2_provider_jwks_uri       = "https://am.nhsdev.auth-ptl.cis2.spineservices.nhs.uk:443/openam/oauth2/realms/root/realms/oidc/connect/jwk_uri"
cis2_client_callback_urls    = ["http://localhost:3000/auth-callback"]
cis2_client_signout_urls     = ["http://localhost:3000"]
virus_scanner_is_stubbed     = "true"
