resource "aws_amplify_app" "doc-store-ui" {
  name = "DocStoreUi"

  # Redirects for Single Page Web Apps (SPA)
  # https://docs.aws.amazon.com/amplify/latest/userguide/redirects.html#redirects-for-single-page-web-apps-spa
  custom_rule {
    source = "</^[^.]+$|\\.(?!(css|gif|ico|jpg|js|png|txt|svg|woff|ttf|map|json)$)([^.]+$)/>"
    status = "200"
    target = "/index.html"
  }

  count = var.cloud_only_service_instances
}

resource "aws_amplify_branch" "main" {
  app_id      = aws_amplify_app.doc-store-ui[0].id
  branch_name = "main"

  framework = "React"
  stage     = "PRODUCTION"

  count = var.cloud_only_service_instances
}

output "amplify_app_ids" {
  value = aws_amplify_app.doc-store-ui[*].id
}
