data "aws_ssm_parameter" "basic_auth_password" {
  name = "/prs/${var.environment}/user-input/basic-auth-password"
}

resource "aws_amplify_app" "doc-store-ui" {
  name = "${terraform.workspace}-DocStoreUi"

  # Redirects for Single Page Web Apps (SPA)
  # https://docs.aws.amazon.com/amplify/latest/userguide/redirects.html#redirects-for-single-page-web-apps-spa
  custom_rule {
    source = "</^[^.]+$|\\.(?!(css|gif|ico|jpg|js|png|txt|svg|woff|ttf|map|json)$)([^.]+$)/>"
    status = "200"
    target = "/index.html"
  }

  enable_basic_auth      = var.enable_basic_auth
  basic_auth_credentials = base64encode("${terraform.workspace}:${data.aws_ssm_parameter.basic_auth_password.value}")

  count = var.cloud_only_service_instances
}

resource "aws_amplify_branch" "main" {
  app_id      = aws_amplify_app.doc-store-ui[0].id
  branch_name = terraform.workspace == "dev" || terraform.workspace == "pre-prod" || terraform.workspace == "prod" ? "main" : terraform.workspace

  framework = "React"
  stage     = "PRODUCTION"

  count = var.cloud_only_service_instances
}
