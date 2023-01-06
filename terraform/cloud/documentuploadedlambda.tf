module "document_uploaded_lambda" {
  source = "../modules/document_uploaded_lambda"
  lambda_execution_role_arn = module.lambda_iam_role.lambda_execution_role_arn
  lambda_jar_filename = var.lambda_jar_filename
  env_vars = {
    SQS_QUEUE_URL = aws_sqs_queue.document-store.url
  }
  s3_bucket_arn = aws_s3_bucket.document_store.arn
}