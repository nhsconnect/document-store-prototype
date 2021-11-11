resource "aws_cognito_user_pool" "pool" {
  name = "doc-store-user-pool"

  admin_create_user_config {
    allow_admin_create_user_only = true
  }

  password_policy {
    minimum_length = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers = true
    require_symbols = true
  }
  count = var.cloud_only_service_instances
}

resource "aws_cognito_user_pool_client" "client" {
  name = "doc-store-user-pool-client"

  user_pool_id = aws_cognito_user_pool.pool[0].id

  count = var.cloud_only_service_instances
}

output "cognito_user_pool_ids" {
  value = aws_cognito_user_pool.pool[*].id
}

output "cognito_user_pool_domain" {
  value = aws_cognito_user_pool.pool[*].domain
}

output "cognito_client_ids" {
  value = aws_cognito_user_pool_client.client[*].id
}

