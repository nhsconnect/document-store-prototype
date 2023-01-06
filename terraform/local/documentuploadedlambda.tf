module "document_uploaded_lambda" {
  source = "../modules/document_uploaded_lambda"
  lambda_execution_role_arn = module.lambda_iam_role.lambda_execution_role_arn
  lambda_jar_filename = var.lambda_jar_filename
  common_env_vars = {}
  s3_bucket_arn = aws_s3_bucket.document_store.arn
}