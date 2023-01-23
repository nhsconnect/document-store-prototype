resource "aws_cognito_user_pool" "pool" {
  name = "doc-store-user-pool"

  admin_create_user_config {
    allow_admin_create_user_only = true
  }

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = true
  }
  count = var.cloud_only_service_instances

  schema {
    name                     = "nhsid_user_orgs"
    attribute_data_type      = "String"
    mutable                  = false
    required                 = false
    developer_only_attribute = false
    string_attribute_constraints {}
  }
}

resource "aws_cognito_user_pool_client" "client" {
  name = "doc-store-user-pool-client"

  user_pool_id = aws_cognito_user_pool.pool[0].id

  count = var.cloud_only_service_instances

  allowed_oauth_flows_user_pool_client = true
  explicit_auth_flows                  = ["ALLOW_ADMIN_USER_PASSWORD_AUTH", "ALLOW_USER_SRP_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]
  allowed_oauth_flows                  = ["code", "implicit"]
  allowed_oauth_scopes                 = ["openid"]
  supported_identity_providers         = var.cognito_oidc_providers
  callback_urls                        = concat(var.cognito_cis2_client_callback_urls, ["https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com/cis2-auth-callback"])
  default_redirect_uri                 = "https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com/cis2-auth-callback"
  logout_urls                          = concat(["https://${aws_amplify_branch.main[0].branch_name}.${aws_amplify_app.doc-store-ui[0].id}.amplifyapp.com"], var.cognito_cis2_client_signout_urls)
}

resource "aws_cognito_user_pool_domain" "domain" {
  domain       = "${var.cognito_domain_prefix}doc-store-user-pool"
  user_pool_id = aws_cognito_user_pool.pool[0].id
  count        = var.cloud_only_service_instances
}

output "cognito_user_pool_ids" {
  value = aws_cognito_user_pool.pool[*].id
}

output "cognito_user_pool_domain" {
  value = [for domain in aws_cognito_user_pool_domain.domain[*].domain : "${domain}.auth.${var.region}.amazoncognito.com"]
}

output "cognito_client_ids" {
  value = aws_cognito_user_pool_client.client[*].id
}

output "cognito_redirect_signin" {
  value = aws_cognito_user_pool_client.client[*].default_redirect_uri
}

output "cognito_redirect_signout" {
  value = var.cloud_only_service_instances > 0 ? element(aws_cognito_user_pool_client.client[*].logout_urls, 0) : []
}

resource "aws_cognito_identity_provider" "cis2_identity_provider" {
  user_pool_id  = aws_cognito_user_pool.pool[0].id
  provider_name = "cis2devoidc"
  provider_type = "OIDC"

  provider_details = {
    authorize_scopes          = "openid associatedorgs nationalrbacaccess"
    client_id                 = var.cognito_cis2_provider_client_id
    client_secret             = var.cognito_cis2_provider_client_secret
    oidc_issuer               = var.cognito_cis2_provider_oidc_issuer
    authorize_url             = var.cognito_cis2_provider_authorize_url
    token_url                 = var.cognito_cis2_provider_token_url
    attributes_url            = var.cognito_cis2_provider_attributes_url
    jwks_uri                  = var.cognito_cis2_provider_jwks_uri
    attributes_request_method = "GET"
  }

  count = var.cloud_only_service_instances
}