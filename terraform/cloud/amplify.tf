resource "aws_amplify_app" "doc-store-ui" {
  name = "DocStoreUi"

  # Redirects for Single Page Web Apps (SPA)
  # https://docs.aws.amazon.com/amplify/latest/userguide/redirects.html#redirects-for-single-page-web-apps-spa
  custom_rule {
    source = "</^[^.]+$|\\.(?!(css|gif|ico|jpg|js|png|txt|svg|woff|ttf|map|json)$)([^.]+$)/>"
    status = "200"
    target = "/index.html"
  }

  enable_basic_auth      = var.enable_basic_auth
  basic_auth_credentials = base64encode("${var.environment}:${var.basic_auth_password}")
}

resource "aws_amplify_branch" "main" {
  app_id      = aws_amplify_app.doc-store-ui.id
  branch_name = "main"

  framework = "React"
  stage     = "PRODUCTION"
}

output "amplify_app_id" {
  value = aws_amplify_app.doc-store-ui.id
}
