resource "aws_amplify_app" "doc-store-ui" {
  name = "DocStoreUi"

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
