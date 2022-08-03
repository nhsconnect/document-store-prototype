resource "aws_s3_bucket" "document_store" {
  bucket_prefix = "document-store-"
  acl           = "private"

  versioning {
    enabled = true
  }
  server_side_encryption_configuration {
    rule {
      bucket_key_enabled = true
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = aws_s3_bucket.document_store.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.document_uploaded_lambda.arn
    events              = ["s3:ObjectCreated:*"]
  }

  depends_on = [aws_lambda_permission.s3_permission_for_document_upload_event]
}

output "document-store-bucket" {
  value = aws_s3_bucket.document_store.bucket
}
